package com.github.mayconr.juoserver.common.event;

import com.github.mayconr.juoserver.game.model.UOMobile;

public record SelectedStatics(UOMobile mobile, int modelId, int x, int y, int z)
        implements GameEvent {}
