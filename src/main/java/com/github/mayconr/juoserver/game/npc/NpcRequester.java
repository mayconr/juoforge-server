package com.github.mayconr.juoserver.game.npc;

import com.github.mayconr.juoserver.game.model.UOPlayer;

import java.util.Objects;

/** Identifies the origin of an NPC creation or removal request; does not change flow scheduling. */
public sealed interface NpcRequester {

    record Player(UOPlayer player) implements NpcRequester {
        public Player {
            Objects.requireNonNull(player, "player");
        }
    }

    record AsyncProcess(String name) implements NpcRequester {
        public AsyncProcess {
            Objects.requireNonNull(name, "name");
            if (name.isBlank()) {
                throw new IllegalArgumentException("Process name cannot be blank");
            }
        }
    }
}
