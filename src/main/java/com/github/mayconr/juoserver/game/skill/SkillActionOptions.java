package com.github.mayconr.juoserver.game.skill;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.Accessors;

/** Configuration captured when scheduling a skill action. */
@Builder
@Getter
@Accessors(fluent = true)
public final class SkillActionOptions {
    private final Integer skillId;
    @Builder.Default
    private final int swings = 1;
    @Builder.Default
    private final double delaySeconds = 0.0;
    private final SkillActionCallback swing;
    @Builder.Default
    private final SkillActionCallback onComplete = swingsExecuted -> {};
}
