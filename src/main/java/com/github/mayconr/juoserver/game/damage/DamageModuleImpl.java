package com.github.mayconr.juoserver.game.damage;

import com.github.mayconr.juoserver.game.item.ItemRequest;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.model.event.MobileDamagedEvent;
import com.github.mayconr.juoserver.game.model.event.MobileDeathEvent;
import com.github.mayconr.juoserver.game.world.ModuleContext;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DamageModuleImpl implements DamageModule {

    private static final int UO_CORPSE_ITEM_ID = 0x2006;
    private final EventBus eventBus;
    private ModuleContext.ItemFacade items;

    @Override
    public void initialize(ModuleContext context) {
        this.items = context.items();
    }

    @Override
    public void applyDamage(DamageRequest request) {
        final var target = request.target();

        int totalDamage = 0;
        for (DamageComponent damage : request.components()) {
            totalDamage += damage.damage();
        }

        int oldHitPoints = target.getHitpoints();
        target.setHitpoints( Math.max(0 , target.getHitpoints() - totalDamage) );


        if (target.getHitpoints() == 0) {
            internalKillHandler(request.target(), request.source(), request.sourceKind());
        } else {
            eventBus.publish(new MobileDamagedEvent(request.source(), target, request.sourceKind(), request.components(), totalDamage, oldHitPoints, target.getHitpoints()));
        }
    }

    @Override
    public void kill(UOMobile target, UOMobile source, DamageSourceKind kind) {
        target.setHitpoints(0);
        target.setStamina(0);
        target.setMana(0);

        internalKillHandler(target, source, kind);
    }

    private void internalKillHandler(UOMobile target, UOMobile source, DamageSourceKind kind) {
        var corpse = items.create(ItemRequest
                .byModelId(UO_CORPSE_ITEM_ID)
                .withHue(target.getHue())
                .withDirection(target.getDirection())
                ,new GroundItemTarget(target), opt -> opt.renderOnCreate(false));
        corpse.setCorpseId(target.getModelId());

        // Target configuration
        target.setAlive(false);

        target.persistentAttributes()
                .add(AttributeKeys.MOBILE_KILLED_BY, source.getSerialId());

        eventBus.publish(new MobileDeathEvent(source, target, kind, corpse));
    }
}
