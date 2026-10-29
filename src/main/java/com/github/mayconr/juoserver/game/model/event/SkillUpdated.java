package com.github.mayconr.juoserver.game.model.event;

import com.github.mayconr.juoserver.game.model.SkillValue;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.infrastructure.eventbus.GameEvent;

public record SkillUpdated(UOPlayer player, SkillValue skill) implements GameEvent { }
