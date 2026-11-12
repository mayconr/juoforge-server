package com.github.mayconr.juoserver.game.item.flow.creation.factory;

import com.github.mayconr.juoserver.game.item.ItemQualityAdjustment;
import com.github.mayconr.juoserver.game.item.flow.creation.ItemCreationContext;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

/**
 * Stores the requested quality and applies the requested adjustment once, before persistence and placement.
 * The material changes only by the difference between the requested and the adjusted quality.
 */
public final class ApplyItemQualityStep extends AbstractFlowStep<ItemCreationContext> {
    public ApplyItemQualityStep() {
        super("ApplyItemQuality");
    }

    @Override
    public StepResult execute(ItemCreationContext context) {
        var data = context.getData();
        var request = context.getRequest();
        float current = request.quality();
        float adjusted = ItemQualityAdjustment.adjust(current, request.qualityAdjustment());
        data.setQuality(adjusted);
        data.setMaterial(ItemQualityAdjustment.rescale(data.getMaterial(), current, adjusted));
        return StepResult.success();
    }
}
