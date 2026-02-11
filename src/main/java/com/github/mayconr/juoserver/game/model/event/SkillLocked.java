package com.github.mayconr.juoserver.game.model.event;

import com.github.mayconr.juoserver.game.event.GameEvent;
import com.github.mayconr.juoserver.game.model.SkillValue;
import com.github.mayconr.juoserver.game.model.UOMobile;

import java.util.List;

public record SkillLocked(UOMobile mobile, List<SkillValue> skills) implements GameEvent {
}
