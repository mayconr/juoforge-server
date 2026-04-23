package com.github.mayconr.juoserver.game.damage;

import com.github.mayconr.juoserver.game.flow.DamageFlowDefinition.DamageContext;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.world.context.ModuleContext;
import com.github.mayconr.juoserver.game.world.context.ModuleContext.FlowFacade;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import lombok.RequiredArgsConstructor;

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

        flows.execute(new DamageContext(source, target, sourceKind, components));
        /*
        final var target = request.target();

        int totalDamage = 0;
        for (DamageComponent damage : request.components()) {
            totalDamage += damage.damage();
        }

        int oldHitPoints = target.getHitpoints();
        target.setHitpoints( Math.max(0 , target.getHitpoints() - totalDamage) );

        if (target.getHitpoints() == 0) {
            eventBus.publish(new LethalDamageEvent(request.source(), request.target(), request.sourceKind()));
        } else {
            eventBus.publish(new MobileDamagedEvent(request.source(), target, request.sourceKind(), request.components(), totalDamage, oldHitPoints, target.getHitpoints()));
        }*/
    }

    @Override
    public void kill(UOMobile target, UOMobile source, DamageSourceKind kind) {
        target.setHitpoints(0);
        target.setStamina(0);
        target.setMana(0);

        eventBus.publish(new LethalDamageEvent(source, target, kind));
    }

}
