package com.github.mayconr.juoserver.game.damage.flow.damage;

import com.github.mayconr.juoserver.game.damage.flow.damage.application.ApplyDamageToMobileStep;
import com.github.mayconr.juoserver.game.damage.flow.damage.calculation.CalculateTotalDamageStep;
import com.github.mayconr.juoserver.game.damage.flow.damage.notification.NotifyDamageStep;
import com.github.mayconr.juoserver.game.damage.flow.damage.resolution.CheckLethalDamageStep;
import com.github.mayconr.juoserver.game.mobile.MobileModule;
import com.github.mayconr.juoserver.game.world.context.FlowRegistryFactory;
import com.github.mayconr.juoserver.game.world.context.FlowRegistryFactory.GameInfra;
import com.github.mayconr.juoserver.game.world.context.FlowRegistryFactory.GameModules;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.FlowFactory;

public final class DamageFlowDefinition {

    private DamageFlowDefinition() {
    }

    public static Flow<DamageContext> build(GameModules modules, GameInfra infra) {
        return FlowFactory.<DamageContext>builder()
                .step(new CalculateTotalDamageStep())
                .step(new ApplyDamageToMobileStep())
                .step(new CheckLethalDamageStep(modules.mobile()))
                .step(new NotifyDamageStep(infra.eventBus()))
                .build();
    }

}
