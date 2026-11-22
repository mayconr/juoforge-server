package com.github.mayconr.juoserver.game.item.flow.flip;

import com.github.mayconr.juoserver.game.model.ContainerLocation;
import com.github.mayconr.juoserver.game.model.GroundLocation;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.storage.WorldStorage;

/**
 * Accepts flippable items on the ground or in a container. Equipped items are drawn by the client from
 * their animation, so their model is not flipped.
 */
final class ValidateFlippableItemStep extends AbstractFlowStep<FlipItemContext> {
    ValidateFlippableItemStep() {
        super("validate-flippable-item");
    }

    @Override
    public StepResult execute(FlipItemContext context) {
        var item = context.getItem();
        var template = item.getTemplate();
        if (template == null || !template.flippable()) {
            return StepResult.failure("ITEM_NOT_FLIPPABLE", "Item template does not allow flipping");
        }
        return switch (item.getCurrentLocation()) {
            case GroundLocation ground -> StepResult.success();
            case ContainerLocation inside -> StepResult.success();
            default -> StepResult.failure("ITEM_FLIP_LOCATION", "Only items on the ground or in containers can be flipped");
        };
    }
}
