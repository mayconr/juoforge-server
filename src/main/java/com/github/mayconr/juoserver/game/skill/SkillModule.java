package com.github.mayconr.juoserver.game.skill;

import com.github.mayconr.juoserver.game.model.SkillValue;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.world.ModuleOperationException;
import com.github.mayconr.juoserver.game.world.WorldModule;

import java.util.Collection;
import java.util.Map;

public interface SkillModule extends WorldSkill, WorldModule {
    /** Atomically reserves a validated action; returns false if the player is already busy. */
    boolean reserveSkillAction(UOPlayer player, SkillAction action);

    /** Returns a snapshot of the current per-player action reservations. */
    Map<Integer, SkillAction> getSkillActions();

    /** Releases the reservation only if it still refers to the given action. */
    void releaseSkillAction(int playerSerial, SkillAction action);

    /**
     * Updates the player's skill locks and publishes the resulting skill values.
     *
     * @param player the player whose skill locks will be updated
     * @param skills the skill identifiers and lock states to apply
     * @throws ModuleOperationException
     *         if the skill lock flow fails
     */
    void sendSkillsLock(UOPlayer player, Collection<SkillValue> skills);

}
