package com.github.mayconr.juoserver.game.skill;

import com.github.mayconr.juoserver.game.model.SkillValue;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.world.WorldModule;

import java.util.Collection;

public interface SkillModule extends WorldSkill, WorldModule {
    /** Reserves an action after the perform-skill flow has validated it. */
    boolean scheduleSkillAction(UOPlayer player, SkillActionOptions options);

    /**
     * Updates the player's skill locks and publishes the resulting skill values.
     *
     * @param player the player whose skill locks will be updated
     * @param skills the skill identifiers and lock states to apply
     * @throws com.github.mayconr.juoserver.game.world.ModuleOperationException
     *         if the skill lock flow fails
     */
    void sendSkillsLock(UOPlayer player, Collection<SkillValue> skills);

}
