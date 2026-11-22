package com.github.mayconr.juoserver.game.ui.flow.singleclick;

import com.github.mayconr.juoserver.game.messaging.MessageModule;
import com.github.mayconr.juoserver.game.messaging.template.MessageStyleTemplate;
import com.github.mayconr.juoserver.game.model.TextType;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

final class SelectObjectLabelStyleStep extends AbstractFlowStep<SingleClickContext> {
    private static final MessageStyleTemplate LABEL_STYLE = new MessageStyleTemplate("LABEL", TextType.LABEL, 0x03B2, 3);

    private final MessageModule messages;

    SelectObjectLabelStyleStep(MessageModule messages) {
        super("select-object-label-style");
        this.messages = messages;
    }

    @Override
    public StepResult execute(SingleClickContext context) {
        var style = LABEL_STYLE;
        if (context.getObject() instanceof UOMobile mobile && mobile.getNotoriety() != null) {
            style = messages.findStyle("NOTORIETY_" + mobile.getNotoriety().name()).orElse(LABEL_STYLE);
        }
        context.setMessageStyle(style);
        return StepResult.success();
    }
}
