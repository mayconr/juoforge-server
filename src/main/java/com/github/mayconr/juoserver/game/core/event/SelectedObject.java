package com.github.mayconr.juoserver.game.core.event;

import com.github.mayconr.juoserver.game.core.model.UOMobile;

public record SelectedObject(UOMobile mobile, int serialId, int x, int y, int z)
        implements GameEvent {}
