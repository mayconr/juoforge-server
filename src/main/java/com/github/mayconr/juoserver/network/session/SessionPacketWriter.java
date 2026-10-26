package com.github.mayconr.juoserver.network.session;

import com.github.mayconr.juoserver.network.packet.Packet;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;

import java.util.concurrent.TimeUnit;

/** Network output for one session. Scheduling and flush boundaries are explicit. */
public final class SessionPacketWriter {
    @FunctionalInterface
    public interface PacketOperation {
        void run(PacketOutput packets);
    }

    private final PacketOutput output = new PacketOutput() {
        @Override
        public void write(Packet packet) {
            requireEventLoop();
            SessionPacketWriter.this.write(packet);
        }

        @Override
        public void writeAndFlush(Packet packet) {
            requireEventLoop();
            SessionPacketWriter.this.writeAndFlush(packet);
        }

        @Override
        public void flush() {
            requireEventLoop();
            SessionPacketWriter.this.flush();
        }
    };

    private final Channel channel;
    private volatile boolean closed;
    private final ChannelGroup channelGroup;

    SessionPacketWriter(Channel channel, ChannelGroup channelGroup) {
        this.channel = channel;
        this.channelGroup = channelGroup;
    }

    private void requireEventLoop() {
        if (!channel.eventLoop().inEventLoop()) {
            throw new IllegalStateException("Packet output must be used on the channel event loop");
        }
    }

    void close() {
        closed = true;
    }

    public boolean isActive() {
        return !closed && channel.isActive();
    }

    public void write(Packet packet) {
        onEventLoop(() -> channel.write(packet));
    }

    public void writeAndFlush(Packet packet) {
        onEventLoop(() -> channel.writeAndFlush(packet));
    }

    public void flush() {
        onEventLoop(channel::flush);
    }

    void broadcast(Packet packet) {
        onEventLoop(() -> channelGroup.write(packet));
    }

    void broadcastAndFlush(Packet packet) {
        onEventLoop(() -> channelGroup.writeAndFlush(packet));
    }

    /** Writes inline on the event loop; otherwise queues output in caller order. */
    private void onEventLoop(Runnable operation) {
        if (!isActive()) {
            return;
        }
        if (channel.eventLoop().inEventLoop()) {
            operation.run();
        } else {
            enqueue(packets -> operation.run());
        }
    }

    /** Enqueues the entire operation as one task, even when already on the event loop. */
    public void enqueue(PacketOperation operation) {
        if (!isActive()) {
            return;
        }
        channel.eventLoop().execute(() -> {
            if (isActive()) {
                operation.run(output);
            }
        });
    }

    public void schedule(PacketOperation operation, long delay, TimeUnit unit) {
        if (!isActive()) {
            return;
        }
        channel.eventLoop().schedule(() -> {
            if (isActive()) {
                operation.run(output);
            }
        }, delay, unit);
    }
}
