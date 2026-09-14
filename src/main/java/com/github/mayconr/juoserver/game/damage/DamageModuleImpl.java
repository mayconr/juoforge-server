package com.github.mayconr.juoserver.game.damage;

import com.github.mayconr.juoserver.game.damage.flow.damage.DamageContext;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.world.context.ModuleContext;
import com.github.mayconr.juoserver.game.world.context.ModuleContext.FlowFacade;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class DamageModuleImpl implements DamageModule {

    private final EventBus eventBus;
    private FlowFacade flows;

    @Override
    public void initialize(ModuleContext context) {
        this.flows = context.flows();
    }

    @Override
    public void applyDamage(DamageRequest request) {
        var source = request.source();
        var target = request.target();
        var sourceKind = request.sourceKind();
        var components = request.components();

        flows.execute(DamageContext.of(source, target, sourceKind, components));
    }

    @Override
    public void kill(UOMobile target, UOMobile source, DamageSourceKind kind) {
        flows.execute(DamageContext.of(source, target, kind, List.of(new DamageComponent(DamageType.PHYSICAL, Integer.MAX_VALUE))));
    }

}
