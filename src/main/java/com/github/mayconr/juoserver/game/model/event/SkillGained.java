package com.github.mayconr.juoserver.game.model.event;

import com.github.mayconr.juoserver.infrastructure.eventbus.GameEvent;
import com.github.mayconr.juoserver.game.model.SkillValue;
import com.github.mayconr.juoserver.game.model.UOMobile;

public record SkillGained(UOMobile mobile, SkillValue skill) implements GameEvent {
}
