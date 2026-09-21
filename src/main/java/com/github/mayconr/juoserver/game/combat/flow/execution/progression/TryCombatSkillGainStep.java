package com.github.mayconr.juoserver.game.combat.flow.execution.progression;

import com.github.mayconr.juoserver.game.combat.flow.execution.CombatExecutionContext;
import com.github.mayconr.juoserver.game.combat.progression.CombatSkillGainContext;
import com.github.mayconr.juoserver.game.combat.progression.CombatSkillGainPolicy;
import com.github.mayconr.juoserver.game.skill.SkillModule;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import java.util.List;
import java.util.Objects;

/** Resolves all gain attempts before applying them, then delegates to the skill module. */
public class TryCombatSkillGainStep extends AbstractFlowStep<CombatExecutionContext> {
    private final SkillModule skills;
    private final CombatSkillGainPolicy policy;

    public TryCombatSkillGainStep(SkillModule skills, CombatSkillGainPolicy policy) {
        super("TryCombatSkillGainStep");
        this.skills = Objects.requireNonNull(skills);
        this.policy = Objects.requireNonNull(policy);
    }

    @Override
    public StepResult execute(CombatExecutionContext context) {
        var gainContext = new CombatSkillGainContext(
                context.getSession().getAttacker(), context.getSession().getTarget(),
                context.getWeaponStyle(), context.getDefenderWeaponStyle(),
                Objects.requireNonNull(context.getHitResult(), "Hit result is not resolved"));
        var attempts = List.copyOf(policy.resolve(gainContext));

        for (var attempt : attempts) {
            skills.tryGain(attempt.beneficiary(), attempt.skillId(), attempt.difficulty(), attempt.gainContext());
        }
        return StepResult.success();
    }
}
