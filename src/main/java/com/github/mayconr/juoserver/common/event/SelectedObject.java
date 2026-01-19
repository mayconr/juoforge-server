package com.github.mayconr.juoserver.common.event;

import com.github.mayconr.juoserver.game.model.UOMobile;

public record SelectedObject(UOMobile mobile, int serialId, int x, int y, int z)
        implements GameEvent {}
