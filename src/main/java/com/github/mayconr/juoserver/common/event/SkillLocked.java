package com.github.mayconr.juoserver.common.event;

import com.github.mayconr.juoserver.game.model.SkillValue;
import com.github.mayconr.juoserver.game.model.UOMobile;

public record SkillLocked(UOMobile mobile, SkillValue value) implements GameEvent {
}
