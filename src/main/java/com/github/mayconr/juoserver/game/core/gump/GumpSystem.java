package com.github.mayconr.juoserver.game.core.gump;

import com.github.mayconr.juoserver.game.core.model.UOMobile;

public interface GumpSystem {

    <T> void send(UOMobile mobile, DeclarativeGumpUI gumpUI, GumpHandler handler);
}
