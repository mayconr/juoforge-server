package com.github.mayconr.juoserver.game.mobile.flow.death.transfer;

import com.github.mayconr.juoserver.game.item.ItemModule;
import com.github.mayconr.juoserver.game.mobile.MobileModule;
import com.github.mayconr.juoserver.game.mobile.flow.death.DeathContext;
import com.github.mayconr.juoserver.game.model.ItemFlag;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.Random;

@Slf4j
public class MoveItemsToCorpseStep extends AbstractFlowStep<DeathContext> {

    private final MobileModule mobileModule;
    private final ItemModule itemModule;
    private final RealmStorage storage;

    public MoveItemsToCorpseStep(MobileModule mobileModule, ItemModule itemModule, RealmStorage storage) {
        super("move_item_corpse_step");
        this.mobileModule = mobileModule;
        this.itemModule = itemModule;
        this.storage = storage;
    }

    @Override
    public StepResult execute(DeathContext context) {
        var victim = context.getVictim();
        var equippedItems = victim.getEquippedItems().values();
        var corpse = Objects.requireNonNull(context.getCorpse(), "Corpse");

        var random = new Random();
        for (Integer itemSerial : equippedItems) {
            var item = storage.getItem(itemSerial).orElseThrow(()->new IllegalStateException("Item Not Found"));
            if (item.getFlags().contains(ItemFlag.MOUNT)) {
                itemModule.deleteItem(item);
                log.debug("Mount item {} has been deleted", item);
                continue;
            }

            if (mobileModule.unequipItem(victim, item)) {
                item.setX(random.nextInt(75 - 20) + 20);
                item.setY(random.nextInt(165 - 85) + 85);
                corpse.addEquippedItem(item);
            }
        }
        return StepResult.success();
    }
}
