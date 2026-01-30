package com.github.mayconr.juoserver.common.event;

import com.github.mayconr.juoserver.game.model.UOMobile;

public record HelpButtonPressed(UOMobile mobile) implements GameEvent {
}
