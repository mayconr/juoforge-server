package com.github.mayconr.juoserver.game.model.event;

import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.infrastructure.eventbus.GameEvent;

public record MobileGoldChanged(UOMobile mobile, int oldBalance, int newBalance) implements GameEvent {
}
