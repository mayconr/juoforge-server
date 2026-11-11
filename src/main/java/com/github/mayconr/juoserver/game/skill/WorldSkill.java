package com.github.mayconr.juoserver.game.skill;

import com.github.mayconr.juoserver.game.model.SkillGainContext;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UOPlayer;

public interface WorldSkill {
    /** Persistent player preference for the number of swings per action; defaults to 1. */
    String SWINGS_PROPERTY = "skill.swings";

    /** Sets the persistent swing count for future actions, clamped to at least 1. */
    void setSwingCount(UOPlayer player, int swingCount);

    /** Sets the base skill, clamped to [0, cap], regardless of its lock, and notifies the player.
     * The value uses skill points (50.0 means 50.0), not protocol tenths.
     */
    void setSkill(UOPlayer player, int skillId, double value);

    /**
     * Reserves one action per player. Returns false when the player already has an action.
     * The optional swingCount overrides the persistent skill.swings preference; the default and minimum are 1.
     * The first swing runs on the next update, followed by a fixed second between swings.
     * At most one swing runs per update.
     * The player position is captured when the action is requested; if the player leaves that tile,
     * the remaining swings are cancelled and onComplete runs with the swings already executed.
     * Callbacks run on the game loop.
     */
    boolean startSkillAction(UOPlayer player, SkillActionOptions options);

    /**
     * Requests the end of the player's active skill action. Returns false when there is no active action.
     * The request takes effect on the next update: the remaining swings are skipped, the action is released
     * and onComplete runs once with the swings already executed. A swing already running is not interrupted,
     * so the request may be made from inside the onSwing callback.
     */
    boolean stopSkillAction(UOPlayer player);

    boolean hasActiveAction(UOPlayer player);

    void useSkill(UOPlayer player, int skillId);

    /**
     * Attempts to increase a skill through the skill gain flow.
     *
     * <p><strong>A higher training intensity makes skill progression easier:</strong>
     * it increases the gain chance up to the configured maximum and increases
     * the amount awarded on a successful roll within the formula's limits.
     * Lower training intensity can reduce the gain chance to zero. This parameter
     * controls skill progression, not the chance of succeeding at the activity.</p>
     *
     * <p>Gains respect the skill's lock and individual cap. Fractional gains
     * accumulate; notification occurs only when the client-visible tenth increases.</p>
     *
     * @param mobile the mobile whose skill may increase
     * @param skillId the skill identifier
     * @param trainingIntensity the activity's training intensity; higher values favor progression
     * @param context contextual attributes for the gain attempt
     */
    void tryGain(UOMobile mobile, int skillId, double trainingIntensity, SkillGainContext context);
}
