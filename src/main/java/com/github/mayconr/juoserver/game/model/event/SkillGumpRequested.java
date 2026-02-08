package com.github.mayconr.juoserver.game.model.event;

import com.github.mayconr.juoserver.game.event.GameEvent;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UOPlayer;

public record SkillGumpRequested(UOPlayer player, UOMobile skillsOf) implements GameEvent {
}
