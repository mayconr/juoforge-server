package com.github.mayconr.juoserver.game.model.event;

import com.github.mayconr.juoserver.infrastructure.eventbus.GameEvent;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOPlayer;

public record ItemStacked(UOPlayer player, UOItem target, UOItem dropped, StackDestination destination) implements GameEvent {

    public enum StackDestination {
        GROUND, CONTAINER;
    }

}
