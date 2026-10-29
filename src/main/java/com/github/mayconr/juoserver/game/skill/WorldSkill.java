package com.github.mayconr.juoserver.game.skill;

import com.github.mayconr.juoserver.game.model.SkillGainContext;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UOPlayer;

public interface WorldSkill {
    /** Sets the base skill, clamped to [0, cap], regardless of its lock, and notifies the player.
     * The value uses skill points (50.0 means 50.0), not protocol tenths.
     */
    void setSkill(UOPlayer player, int skillId, double value);

    /**
     * Reserves one action per player. Returns false when the player already has an action.
     * The first swing runs on the next world update; subsequent swings use simulation seconds.
     * At most one swing runs per update. Callbacks run on the game loop, including completion.
     */
    boolean startAction(UOPlayer player, int swings, double delaySeconds, SkillActionCallback swing, SkillActionCallback onComplete);

    default boolean startAction(UOPlayer player, int swings, double delaySeconds, SkillActionCallback swing) {
        return startAction(player, swings, delaySeconds, swing, swingsExecuted -> {});
    }

    boolean hasActiveAction(UOPlayer player);

    void useSkill(UOPlayer player, int skillId);

    void tryGain(UOMobile mobile, int skillId, double difficulty, SkillGainContext context);
}
