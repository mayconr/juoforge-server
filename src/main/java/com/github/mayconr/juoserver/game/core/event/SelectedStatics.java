package com.github.mayconr.juoserver.game.core.event;

import com.github.mayconr.juoserver.game.core.model.UOMobile;

public record SelectedStatics(UOMobile mobile, int modelId, int x, int y, int z)
        implements GameEvent {}
