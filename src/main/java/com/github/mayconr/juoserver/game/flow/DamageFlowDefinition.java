package com.github.mayconr.juoserver.game.flow;

import com.github.mayconr.juoserver.game.damage.flow.damage.ApplyDamageToMobileStep;
import com.github.mayconr.juoserver.game.damage.flow.damage.CalculateTotalDamageStep;
import com.github.mayconr.juoserver.game.damage.flow.damage.CheckLethalDamageStep;
import com.github.mayconr.juoserver.game.damage.flow.damage.NotifyDamageStep;
import com.github.mayconr.juoserver.game.mobile.MobileModule;
import com.github.mayconr.juoserver.game.model.DamageComponent;
import com.github.mayconr.juoserver.game.model.DamageSourceKind;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.FlowContext;
import com.github.mayconr.juoserver.infrastructure.flow.FlowFactory;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import java.util.List;

public final class DamageFlowDefinition {

    @EqualsAndHashCode(callSuper = true)
    @Data
    @RequiredArgsConstructor
    public static class DamageContext extends FlowContext {
        private final UOMobile source;
        private final UOMobile target;
        private final DamageSourceKind sourceKind;
        private final List<DamageComponent> components;
        private int totalDamage;
        private int oldHp;
        private int newHp;
        private boolean lethal;
    }

    private DamageFlowDefinition() {
    }

    public static Flow<DamageContext> build(MobileModule mobileModule, EventBus eventBus) {
        return FlowFactory.<DamageContext>builder()
                .step(new CalculateTotalDamageStep())
                .step(new ApplyDamageToMobileStep())
                .step(new CheckLethalDamageStep(mobileModule))
                .step(new NotifyDamageStep(eventBus))
                .build();
    }

}
