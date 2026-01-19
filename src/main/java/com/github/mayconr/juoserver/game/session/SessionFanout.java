package com.github.mayconr.juoserver.game.session;

import com.github.mayconr.juoserver.game.model.UOMobile;

import java.util.Optional;
import java.util.function.Predicate;

public interface SessionFanout {

    void writeAndFlush(Object message);

    void writeAndFlush(Object message, Predicate<SessionOutbound> predicate);

    void write(Object message);

    void flush();

    Optional<SessionOutbound> getSessionOutbound(UOMobile mobile);
}
