package com.github.mayconr.juoserver.game.model.event;

import com.github.mayconr.juoforge.reader.skill.Skill;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.infrastructure.eventbus.GameEvent;

public record UseSkillRequested(UOMobile mobile, Skill skill) implements GameEvent {

}
