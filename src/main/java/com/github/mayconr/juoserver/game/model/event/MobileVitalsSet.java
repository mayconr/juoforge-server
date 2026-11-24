package com.github.mayconr.juoserver.game.model.event;

import com.github.mayconr.juoserver.game.mobile.VitalType;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.infrastructure.eventbus.GameEvent;

public record MobileVitalsSet(UOMobile mobile, VitalType type, int max, int value) implements GameEvent {
}
