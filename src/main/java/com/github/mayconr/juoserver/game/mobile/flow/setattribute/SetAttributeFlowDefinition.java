package com.github.mayconr.juoserver.game.mobile.flow.setattribute;

import com.github.mayconr.juoserver.game.mobile.shared.NotifyMobileStatusStep;
import com.github.mayconr.juoserver.game.ui.WorldUI;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.FlowFactory;

public final class SetAttributeFlowDefinition {
    private SetAttributeFlowDefinition() {}

    public static Flow<SetAttributeContext> build(WorldUI ui) {
        return FlowFactory.<SetAttributeContext>builder()
                .step(new ApplyAttributeStep())
                .step(new NotifyMobileStatusStep<>(ui))
                .build();
    }
}
