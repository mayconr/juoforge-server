package com.github.mayconr.juoserver.game.session;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.EventExecutor;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@EqualsAndHashCode
@RequiredArgsConstructor
public final class SessionOutbound {

    @EqualsAndHashCode.Include
    private final ChannelHandlerContext ctx;
    private final Channel channel;
    private final EventExecutor executor;
    private final SessionAttributes attributes = new SessionAttributes();

    public ChannelHandlerContext getCtx() {
        return ctx;
    }

    public SessionAttributes attr() {
        return attributes;
    }

    public void write(Object packet) {
        run(() -> channel.write(packet));
    }

    public void flush() {
        run(channel::flush);
    }

    public void writeAndFlush(Object packet) {
        run(() -> channel.writeAndFlush(packet));
    }

    public void onChannelClosed(Runnable task) {
        channel.closeFuture().addListener(future -> run(task));
    }

    private void run(Runnable task) {
        if (!channel.isActive()) {
            return;
        }

        if (executor.inEventLoop()) {
            task.run();
        } else {
            executor.execute(task);
        }
    }
}
