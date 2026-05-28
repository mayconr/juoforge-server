package com.github.mayconr.juoserver.game.combat.flow.execution;

import com.github.mayconr.juoserver.game.combat.flow.execution.calculation.CalculateSwingFramesStep;
import com.github.mayconr.juoserver.game.combat.flow.execution.damage.ApplyDamageStep;
import com.github.mayconr.juoserver.game.combat.flow.execution.notify.BroadcastAttackAnimationStep;
import com.github.mayconr.juoserver.game.combat.flow.execution.resolver.WeaponResolverStep;
import com.github.mayconr.juoserver.game.combat.flow.execution.resolver.WeaponStyleResolverStep;
import com.github.mayconr.juoserver.game.combat.flow.execution.swing.CombatHitFrameDelayStep;
import com.github.mayconr.juoserver.game.combat.flow.execution.validation.ValidateTargetDistanceStep;
import com.github.mayconr.juoserver.game.world.context.FlowRegistryFactory;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.FlowBuilder;
import com.github.mayconr.juoserver.infrastructure.flow.FlowFactory;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;

public class CombatExecutionFlow {
    private CombatExecutionFlow() {
    }

    public static Flow<CombatExecutionContext> build(FlowRegistryFactory.GameModules modules, FlowRegistryFactory.GameInfra infra) {
        return FlowFactory.<CombatExecutionContext>builder()
            .step(new WeaponResolverStep(infra.storage()))
            .step(new ValidateTargetDistanceStep())
            // Calculate hit and anim frames
            .step(new CalculateSwingFramesStep())
            // Identify combat type
            .appendGroup("WeaponExtract", weaponExtract(infra.storage()))
            // Sends anim event
            .step(new BroadcastAttackAnimationStep(infra.eventBus()))
            // Wait for hit frame
            .step(new CombatHitFrameDelayStep())
            .step(new ApplyDamageStep(modules.damage()))
            .build();
    }

    private static FlowBuilder<CombatExecutionContext> weaponExtract(RealmStorage storage) {
        return FlowFactory.<CombatExecutionContext>builder()

                .step(new WeaponStyleResolverStep());
    }
}
