package com.github.mayconr.juoserver.game.session;

import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UOPlayer;

import java.util.Optional;
import java.util.function.Predicate;

public interface SessionFanout {

    void writeAndFlush(Object message);

    void writeAndFlush(Object message, Predicate<UOPlayer> predicate);

    void write(Object message);

    void write(Object message, Predicate<UOMobile> predicate);

    void flush();

    Optional<SessionOutbound> getSessionOutbound(UOMobile mobile);
}
