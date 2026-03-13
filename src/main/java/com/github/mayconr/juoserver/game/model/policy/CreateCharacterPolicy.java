package com.github.mayconr.juoserver.game.model.policy;

import com.github.mayconr.juoserver.game.player.PlayerDetails;
import com.github.mayconr.juoserver.infrastructure.policy.ActionPolicy;

public record CreateCharacterPolicy(PlayerDetails details) implements ActionPolicy {
}
