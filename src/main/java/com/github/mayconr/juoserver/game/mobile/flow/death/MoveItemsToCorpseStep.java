package com.github.mayconr.juoserver.game.mobile.flow.death;

import com.github.mayconr.juoserver.game.flow.DeathFlowDefinition;
import com.github.mayconr.juoserver.game.item.ItemModule;
import com.github.mayconr.juoserver.game.mobile.MobileModule;
import com.github.mayconr.juoserver.game.model.ItemFlag;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.FlowPhase;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.Random;

@Slf4j
public class MoveItemsToCorpseStep extends AbstractFlowStep<DeathFlowDefinition.DeathContext> {

    private final MobileModule mobileModule;
    private final ItemModule itemModule;

    public MoveItemsToCorpseStep(MobileModule mobileModule, ItemModule itemModule) {
        super("move_item_corpse_step", 300, FlowPhase.CORE);
        this.mobileModule = mobileModule;
        this.itemModule = itemModule;
    }

    @Override
    public StepResult execute(DeathFlowDefinition.DeathContext context) {
        var victim = context.getVictim();
        var equippedItems = victim.getEquippedItems().values();
        var corpse = Objects.requireNonNull(context.getCorpse(), "Corpse");

        var random = new Random();
        for (UOItem item : equippedItems) {
            if (item.getFlags().contains(ItemFlag.MOUNT)) {
                itemModule.deleteItem(item);
                System.out.println(item.getMountName());
                log.debug("Mount item {} has been deleted", item);
                continue;
            }

            if (mobileModule.unequipItem(victim, item)) {
                item.setX(random.nextInt(75 - 20) + 20);
                item.setY(random.nextInt(165 - 85) + 85);
                corpse.addEquippedItem(item);
            }
        }
        return StepResult.CONTINUE;
    }
}
