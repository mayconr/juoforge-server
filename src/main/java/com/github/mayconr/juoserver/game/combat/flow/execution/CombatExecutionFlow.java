package com.github.mayconr.juoserver.game.combat.flow.execution;

import com.github.mayconr.juoserver.game.combat.flow.execution.calculation.CalculateSwingFramesStep;
import com.github.mayconr.juoserver.game.combat.flow.execution.calculation.CheckCombatHitStep;
import com.github.mayconr.juoserver.game.combat.flow.execution.calculation.StopOnCombatMissStep;
import com.github.mayconr.juoserver.game.combat.flow.execution.progression.TryCombatSkillGainStep;
import com.github.mayconr.juoserver.game.damage.shared.CalculateDamageStep;
import com.github.mayconr.juoserver.game.combat.flow.execution.damage.ApplyDamageStep;
import com.github.mayconr.juoserver.game.combat.flow.execution.notify.BroadcastAttackAnimationStep;
import com.github.mayconr.juoserver.game.combat.flow.execution.resolver.CombatTypeResolverStep;
import com.github.mayconr.juoserver.game.combat.flow.execution.resolver.ResolveCombatMaxDistanceStep;
import com.github.mayconr.juoserver.game.combat.flow.execution.resolver.ResolveCombatRadius;
import com.github.mayconr.juoserver.game.combat.flow.execution.resolver.WeaponResolverStep;
import com.github.mayconr.juoserver.game.combat.flow.execution.swing.CombatHitFrameDelayStep;
import com.github.mayconr.juoserver.game.combat.flow.execution.validation.ValidateTargetDistanceStep;
import com.github.mayconr.juoserver.game.world.context.FlowRegistryFactory;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.FlowBuilder;
import com.github.mayconr.juoserver.infrastructure.flow.FlowFactory;

public class CombatExecutionFlow {
    private CombatExecutionFlow() {
    }

    public static Flow<CombatExecutionContext> build(FlowRegistryFactory.GameModules modules, FlowRegistryFactory.GameInfra infra) {
        return FlowFactory.<CombatExecutionContext>builder()
            .appendGroup("CombatSetup", setup(infra))
            .appendGroup("CombatSwing", swing(infra))
            .appendGroup("CombatHitResolution", hitResolution(infra))
            .appendGroup("CombatSkillGain", skillGain(modules, infra))
            .appendGroup("CombatDamage", damage(modules))
            .build();
    }

    private static FlowBuilder<CombatExecutionContext> setup(FlowRegistryFactory.GameInfra infra) {
        return FlowFactory.<CombatExecutionContext>builder()
            .step(new WeaponResolverStep(infra.storage()))
            .step(new CombatTypeResolverStep())
            .step(new ResolveCombatMaxDistanceStep())
            .step(new ResolveCombatRadius())
            .step(new ValidateTargetDistanceStep());
    }

    private static FlowBuilder<CombatExecutionContext> swing(FlowRegistryFactory.GameInfra infra) {
        return FlowFactory.<CombatExecutionContext>builder()
            .step(new CalculateSwingFramesStep())
            .step(new BroadcastAttackAnimationStep(infra.eventBus()))
            .step(new CombatHitFrameDelayStep());
    }

    private static FlowBuilder<CombatExecutionContext> hitResolution(FlowRegistryFactory.GameInfra infra) {
        return FlowFactory.<CombatExecutionContext>builder()
            .step(new CheckCombatHitStep(infra.storage(), infra.rng()));
    }

    private static FlowBuilder<CombatExecutionContext> skillGain(
            FlowRegistryFactory.GameModules modules, FlowRegistryFactory.GameInfra infra) {
        return FlowFactory.<CombatExecutionContext>builder()
            .step(new TryCombatSkillGainStep(modules.skill(), infra.combatSkillGainPolicy()));
    }

    private static FlowBuilder<CombatExecutionContext> damage(FlowRegistryFactory.GameModules modules) {
        return FlowFactory.<CombatExecutionContext>builder()
            .step(new StopOnCombatMissStep())
            .step(new CalculateDamageStep<>())
            .step(new ApplyDamageStep(modules.damage()));
    }

}
