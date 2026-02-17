package com.github.mayconr.juoserver.standard.ai.animal;

import com.github.mayconr.juoserver.game.ai.AIContext;
import com.github.mayconr.juoserver.game.ai.behavior.Behavior;
import com.github.mayconr.juoserver.game.ai.decision.NpcAI;
import com.github.mayconr.juoserver.game.ai.profile.BehaviorProfile;

public class PassiveAnimalAI implements NpcAI {
    @Override
    public String getKey() {
        return "PASSIVE_ANIMAL";
    }

    @Override
    public Behavior decide(AIContext ctx, BehaviorProfile profile) {
        return profile.wander();
    }
}
