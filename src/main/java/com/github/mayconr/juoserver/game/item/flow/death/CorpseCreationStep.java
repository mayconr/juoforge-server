package com.github.mayconr.juoserver.game.item.flow.death;

import com.github.mayconr.juoserver.game.flow.DeathFlowDefinition;
import com.github.mayconr.juoserver.game.item.ItemModule;
import com.github.mayconr.juoserver.game.item.ItemRequest;
import com.github.mayconr.juoserver.game.model.GroundItemTarget;
import com.github.mayconr.juoserver.game.model.UOCorpse;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.FlowPhase;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public class CorpseCreationStep extends AbstractFlowStep<DeathFlowDefinition.DeathContext> {

    private static final int UO_CORPSE_ITEM_ID = 0x2006;
    private final ItemModule itemModule;

    public CorpseCreationStep(ItemModule itemModule) {
        super("corpse_creation_step", 200, FlowPhase.CORE);
        this.itemModule = itemModule;
    }

    @Override
    public StepResult execute(DeathFlowDefinition.DeathContext context) {
        var victim = context.getVictim();

        var corpse = (UOCorpse) itemModule.createItem(
                ItemRequest.byModelId(UO_CORPSE_ITEM_ID)
                        .withHue(victim.getHue())
                        .withDirection(victim.getDirection()),
                new GroundItemTarget(victim), opt -> opt.renderOnCreate(false));
        corpse.setCorpseId(victim.getModelId());
        //corpse.setOwnerSerialId(victim.getSerialId()); // TODO

        // Set context corpse
        context.corpse(corpse);

        return StepResult.CONTINUE;
    }
}
