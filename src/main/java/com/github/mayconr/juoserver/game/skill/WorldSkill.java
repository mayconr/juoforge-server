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
    boolean performSkill(UOPlayer player, SkillActionOptions options);

    boolean hasActiveAction(UOPlayer player);

    void useSkill(UOPlayer player, int skillId);

    /**
     * Attempts to increase a skill through the skill gain flow.
     *
     * <p><strong>A higher difficulty makes skill progression easier:</strong>
     * it increases the gain chance up to the configured maximum and increases
     * the amount awarded on a successful roll within the formula's limits.
     * Lower difficulty can reduce the gain chance to zero. This parameter
     * controls skill progression, not the chance of succeeding at the activity.</p>
     *
     * <p>Gains respect the skill's lock and individual cap. Fractional gains
     * accumulate; notification occurs only when the client-visible tenth increases.</p>
     *
     * @param mobile the mobile whose skill may increase
     * @param skillId the skill identifier
     * @param difficulty the activity's training difficulty; higher values favor progression
     * @param context contextual information about the gain attempt
     */
    void tryGain(UOMobile mobile, int skillId, double difficulty, SkillGainContext context);
}
