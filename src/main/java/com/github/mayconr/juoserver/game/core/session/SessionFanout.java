package com.github.mayconr.juoserver.game.core.session;

public interface SessionFanout {

    void writeAndFlush(Object message);
}
