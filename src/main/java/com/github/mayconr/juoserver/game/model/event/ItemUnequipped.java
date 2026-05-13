package com.github.mayconr.juoserver.game.model.event;

import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.infrastructure.eventbus.GameEvent;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOPlayer;

public record ItemUnequipped(UOMobile mobile, UOItem item) implements GameEvent {
}
