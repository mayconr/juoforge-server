package com.github.mayconr.juoserver.game.model.policy;

import com.github.mayconr.juoserver.game.model.PlayerDetails;
import com.github.mayconr.juoserver.game.policy.ActionPolicy;

public record CreateCharacterPolicy(PlayerDetails details) implements ActionPolicy {
}
