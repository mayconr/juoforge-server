package com.github.mayconr.juoserver.game.skill;

import com.github.mayconr.juoserver.game.model.SkillGainContext;
import com.github.mayconr.juoserver.game.model.UOMobile;

/** A request to attempt a gain, not a guaranteed skill increase. */
public record SkillGainAttempt(
        UOMobile beneficiary,
        int skillId,
        double difficulty,
        SkillGainContext gainContext
) {}
