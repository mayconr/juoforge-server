package com.github.mayconr.juoserver.game.mobile.flow.equip.request;

import com.github.mayconr.juoserver.game.mobile.flow.equip.EquipItemContext;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public class ExtractEquipItemRequestStep extends AbstractFlowStep<EquipItemContext> {

    public ExtractEquipItemRequestStep() {
        super("extract_equip_item_request");
    }

    @Override
    public StepResult execute(EquipItemContext context) {
        final var request = context.getEquipItem();

        context.setItemSerialId(request.getItemSerialId());

        return StepResult.success();
    }
}
