package com.github.mayconr.juoserver.game.item.flow.drop.validation;

import com.github.mayconr.juoserver.game.item.flow.drop.DropItemContext;
import com.github.mayconr.juoserver.game.item.flow.drop.DropItemContext.DropTarget;
import com.github.mayconr.juoserver.game.messaging.MessageModule;
import com.github.mayconr.juoserver.game.mobile.MobileModule;
import com.github.mayconr.juoserver.game.model.EquippedLocation;
import com.github.mayconr.juoserver.game.model.GameMath;
import com.github.mayconr.juoserver.game.model.OrphanLocation;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ValidateInteractionRangeStep extends AbstractFlowStep<DropItemContext> {

    private final MobileModule mobileModule;
    private final MessageModule messageModule;

    public ValidateInteractionRangeStep(MobileModule mobileModule, MessageModule messageModule) {
        super("validate-interaction-range");
        this.mobileModule = mobileModule;
        this.messageModule = messageModule;
    }

    @Override
    public StepResult execute(DropItemContext context) {
        final var player = context.getPlayer();
        var item = context.getItem();


        if (isDroppedOnTheGround(context) && wasEquipped(item)) {
            var isOutOfRange = !GameMath.isInRange(player, context.getDropItem(), 2);
            if (isOutOfRange) {
                log.debug("{} out of range", item.getCurrentLocation());
                mobileModule.equipItem(player, item);
                messageModule.send(player, "{drop.too_far_away}");
                return StepResult.failure(item.getCurrentLocation() + " out of range");
            }
        }

        if (isDroppedIntoContainerOrStack(context)) {
            if (!GameMath.isInRange(player, item, 2)) {
                System.out.println("jogou na bag ou stack. Fora do radios");
            }
        }

        return StepResult.success();
    }

    private boolean isOrphanItem(UOItem item) {
        return item.getCurrentLocation() instanceof OrphanLocation;
    }

    private boolean wasEquipped(UOItem item) {
        return item.getPreviousLocation() instanceof EquippedLocation;
    }

    private boolean isDroppedOnTheGround(DropItemContext context) {
        return DropTarget.GROUND.equals(context.getTarget());
    }

    private boolean isDroppedIntoContainerOrStack(DropItemContext context) {
        return DropTarget.ITEM.equals(context.getTarget());
    }
}
