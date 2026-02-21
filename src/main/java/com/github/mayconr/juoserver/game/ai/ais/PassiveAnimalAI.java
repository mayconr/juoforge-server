package com.github.mayconr.juoserver.game.ai.ais;

import com.github.mayconr.juoserver.game.ai.AIContext;
import com.github.mayconr.juoserver.game.ai.Behavior;
import com.github.mayconr.juoserver.game.ai.AI;
import com.github.mayconr.juoserver.game.ai.BehaviorProfile;

public class PassiveAnimalAI implements AI {
    @Override
    public String getKey() {
        return "PASSIVE_ANIMAL";
    }

    @Override
    public Behavior decide(AIContext ctx, BehaviorProfile profile) {
        return profile.wander();
    }
}
