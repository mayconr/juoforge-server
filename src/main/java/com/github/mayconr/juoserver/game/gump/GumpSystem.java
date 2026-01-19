package com.github.mayconr.juoserver.game.gump;

import com.github.mayconr.juoserver.game.model.UOMobile;

public interface GumpSystem {

    <T> void send(UOMobile mobile, DeclarativeGumpUI gumpUI, GumpHandler handler);
}
