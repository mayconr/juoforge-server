package com.github.mayconr.juoserver.game.model.event;

import com.github.mayconr.juoserver.infrastructure.eventbus.GameEvent;
import com.github.mayconr.juoserver.game.model.AnimationOptions;
import com.github.mayconr.juoserver.game.model.UOMobile;

public record AnimationSent(UOMobile mobile, AnimationOptions options) implements GameEvent {
}
