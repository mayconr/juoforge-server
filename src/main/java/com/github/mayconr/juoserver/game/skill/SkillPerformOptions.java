package com.github.mayconr.juoserver.game.skill;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.Accessors;

/** Configuration captured when scheduling a skill action. */
@Builder
@Getter
@Accessors(fluent = true)
public final class SkillPerformOptions {
    private final Integer skillId;
    /** Optional action-specific override of the persistent skill.swings preference. */
    private final Integer swingCount;
    private final SkillCallback swing;
    @Builder.Default
    private final SkillCallback onComplete = executions -> {};
}
