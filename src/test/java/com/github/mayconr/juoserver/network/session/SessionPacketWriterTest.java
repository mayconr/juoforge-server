package com.github.mayconr.juoserver.network.session;

import com.github.mayconr.juoserver.network.packet.Packet;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.channel.group.ChannelGroup;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SessionPacketWriterTest {
    @Test
    void preservesPacketOrderAndExplicitFlushBoundaries() {
        var flushCounter = new FlushCounter();
        var channel = new EmbeddedChannel(flushCounter);
        try {
            var writer = new SessionPacketWriter(channel, null);
            var first = mock(Packet.class);
            var second = mock(Packet.class);
            writer.enqueue(packets -> {
                packets.write(first);
                packets.write(second);
                assertEquals(0, flushCounter.count);
                packets.flush();
            });
            channel.runPendingTasks();
            assertEquals(1, flushCounter.count);
            assertSame(first, channel.readOutbound());
            assertSame(second, channel.readOutbound());
            assertNull(channel.readOutbound());
        } finally {
            channel.finishAndReleaseAll();
        }
    }

    @Test
    void preservesScheduledDelay() {
        var channel = new EmbeddedChannel();
        try {
            var writer = new SessionPacketWriter(channel, null);
            var packet = mock(Packet.class);
            writer.schedule(packets -> packets.writeAndFlush(packet), 1, TimeUnit.DAYS);
            channel.runScheduledPendingTasks();
            assertNull(channel.readOutbound());
            channel.advanceTimeBy(1, TimeUnit.DAYS);
            channel.runScheduledPendingTasks();
            assertSame(packet, channel.readOutbound());
            assertNull(channel.readOutbound());
        } finally {
            channel.finishAndReleaseAll();
        }
    }

    @Test
    void keepsBroadcastOutputSeparateFromSessionOutput() {
        var channel = new EmbeddedChannel();
        try {
            var group = mock(ChannelGroup.class);
            var writer = new SessionPacketWriter(channel, group);
            var packet = mock(Packet.class);
            writer.broadcast(packet);
            writer.broadcastAndFlush(packet);
            verify(group).write(packet);
            verify(group).writeAndFlush(packet);
            assertNull(channel.readOutbound());
            assertTrue(writer.isActive());
            channel.close();
            assertFalse(writer.isActive());
        } finally {
            channel.finishAndReleaseAll();
        }
    }

    private static class FlushCounter extends ChannelOutboundHandlerAdapter {
        private int count;

        @Override
        public void flush(ChannelHandlerContext ctx) throws Exception {
            count++;
            super.flush(ctx);
        }
    }

    @Test
    void disconnectCancelsQueuedAndDelayedOutputWhileChannelIsStillOpen() {
        var channel = new EmbeddedChannel();
        try {
            var writer = new SessionPacketWriter(channel, null);
            var queued = mock(SessionPacketWriter.PacketOperation.class);
            var delayed = mock(SessionPacketWriter.PacketOperation.class);
            writer.enqueue(queued);
            writer.schedule(delayed, 1, TimeUnit.DAYS);
            writer.close();
            assertTrue(channel.isActive());
            channel.runPendingTasks();
            channel.advanceTimeBy(1, TimeUnit.DAYS);
            channel.runScheduledPendingTasks();
            writer.writeAndFlush(mock(Packet.class));
            assertNull(channel.readOutbound());
            verifyNoInteractions(queued, delayed);
        } finally {
            channel.finishAndReleaseAll();
        }
    }


    @Test
    void runsOperationsOnRealEventLoopAndRejectsDelegateUseOutsideIt() throws Exception {
        var eventLoop = new io.netty.channel.DefaultEventLoop();
        try {
            var channel = mock(io.netty.channel.Channel.class);
            when(channel.eventLoop()).thenReturn(eventLoop);
            when(channel.isActive()).thenReturn(true);
            var writer = new SessionPacketWriter(channel, null);
            var packet = mock(Packet.class);
            var completed = new java.util.concurrent.CompletableFuture<Void>();
            var scheduled = new java.util.concurrent.CompletableFuture<Void>();
            var output = new java.util.concurrent.atomic.AtomicReference<PacketOutput>();
            assertFalse(eventLoop.inEventLoop());
            writer.enqueue(packets -> {
                try {
                    assertTrue(eventLoop.inEventLoop());
                    output.set(packets);
                    packets.write(packet);
                    packets.flush();
                    completed.complete(null);
                } catch (Throwable failure) {
                    completed.completeExceptionally(failure);
                }
            });
            writer.schedule(packets -> {
                try {
                    assertTrue(eventLoop.inEventLoop());
                    packets.writeAndFlush(packet);
                    scheduled.complete(null);
                } catch (Throwable failure) {
                    scheduled.completeExceptionally(failure);
                }
            }, 0, TimeUnit.MILLISECONDS);
            java.util.concurrent.CompletableFuture.allOf(completed, scheduled).get(5, TimeUnit.SECONDS);
            verify(channel).write(packet);
            verify(channel).flush();
            verify(channel).writeAndFlush(packet);
            assertThrows(IllegalStateException.class, () -> output.get().write(packet));
            assertThrows(IllegalStateException.class, () -> output.get().writeAndFlush(packet));
            assertThrows(IllegalStateException.class, () -> output.get().flush());
        } finally {
            eventLoop.shutdownGracefully(0, 0, TimeUnit.MILLISECONDS).syncUninterruptibly();
        }
    }

    @Test
    void allDirectOutputMethodsRunOnEventLoopInSubmissionOrder() throws Exception {
        var eventLoop = new io.netty.channel.DefaultEventLoop();
        try {
            var channel = mock(io.netty.channel.Channel.class);
            var group = mock(ChannelGroup.class);
            when(channel.eventLoop()).thenReturn(eventLoop);
            when(channel.isActive()).thenReturn(true);
            var calls = new java.util.concurrent.CopyOnWriteArrayList<String>();
            var threads = new java.util.concurrent.CopyOnWriteArrayList<Boolean>();
            org.mockito.stubbing.Answer<Object> record = invocation -> {
                threads.add(eventLoop.inEventLoop());
                calls.add(invocation.getMethod().getName());
                return null;
            };
            doAnswer(record).when(channel).write(any());
            doAnswer(record).when(channel).writeAndFlush(any());
            doAnswer(record).when(channel).flush();
            doAnswer(record).when(group).write(any());
            doAnswer(record).when(group).writeAndFlush(any());
            var writer = new SessionPacketWriter(channel, group);
            var packet = mock(Packet.class);
            assertFalse(eventLoop.inEventLoop());
            writer.write(packet);
            writer.flush();
            writer.writeAndFlush(packet);
            writer.broadcast(packet);
            writer.broadcastAndFlush(packet);
            eventLoop.submit(() -> { }).get(5, TimeUnit.SECONDS);
            assertEquals(java.util.List.of("write", "flush", "writeAndFlush", "write", "writeAndFlush"), calls);
            assertEquals(java.util.List.of(true, true, true, true, true), threads);
        } finally {
            eventLoop.shutdownGracefully(0, 0, TimeUnit.MILLISECONDS).syncUninterruptibly();
        }
    }
}
