package com.github.mayconr.juoserver.game.session;

public interface SessionFanout {

    void writeAndFlush(Object message);

    void write(Object message);

    void flush();
}
