package com.github.mayconr.juoserver.game.mobile;

import com.github.mayconr.juoserver.game.item.ItemRequest;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.model.event.MobileDeathEvent;
import com.github.mayconr.juoserver.game.world.ModuleContext;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.Random;

@RequiredArgsConstructor
@Slf4j
public class DeathService {

    private static final int UO_CORPSE_ITEM_ID = 0x2006;
    private final EventBus eventBus;
    private final ItemEquipService itemEquipService;

    private ModuleContext.ItemFacade items;

    public void initialize(ModuleContext context) {
        this.items = context.items();
    }

    public void die(DeathRequest request) {
        log.info("Mobile [{}-{}] death process started", request.victim().getSerialId(), request.victim().getName());
        final var victim = Objects.requireNonNull(request.victim(), "victim is null");
        final var killer = request.killer();
        final var cause = Objects.requireNonNull(request.cause(), "cause is null");

        if (!request.victim().isAlive()) {
            return;
        }
        var corpse = (UOCorpse) items.create(ItemRequest
                        .byModelId(UO_CORPSE_ITEM_ID)
                        .withHue(victim.getHue())
                        .withDirection(victim.getDirection())
                ,new GroundItemTarget(victim), opt -> opt.renderOnCreate(false));
        corpse.setCorpseId(victim.getModelId());
        corpse.setOwnerSerialId(victim.getSerialId());

        var random = new Random();
        for (UOItem item : victim.getEquippedItems().values()) {
            if (itemEquipService.unequipItem(victim, item)) {
                item.setX(random.nextInt(75 - 20) + 20);
                item.setY(random.nextInt(165 - 85) + 85);
                corpse.addEquippedItem(item);
            }
        }

        // Target configuration
        victim.setAlive(false);

        if (killer != null) {
            victim.persistentAttributes()
                    .add(AttributeKeys.MOBILE_KILLED_BY, killer.getSerialId());
        }

        eventBus.publish(new MobileDeathEvent(killer, victim, cause, corpse));
    }

}
