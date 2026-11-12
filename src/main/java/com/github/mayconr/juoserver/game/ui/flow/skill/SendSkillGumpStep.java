package com.github.mayconr.juoserver.game.ui.flow.skill;

import com.github.mayconr.juoserver.game.model.event.SkillGumpRequested;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.storage.WorldStorage;

public final class SendSkillGumpStep extends AbstractFlowStep<SkillGumpContext> {
    private final EventBus events;
    private final WorldStorage storage;

    public SendSkillGumpStep(EventBus events, WorldStorage storage) {
        super("send-skill-gump");
        this.events = events;
        this.storage = storage;
    }

    @Override
    public StepResult execute(SkillGumpContext context) {
        var mobile = storage.mobiles().get(context.getMobileSerial()).orElse(null);
        if (mobile == null) {
            return StepResult.failure("MOBILE_NOT_FOUND", "Mobile not found " + context.getMobileSerial());
        }
        events.publish(new SkillGumpRequested(context.getPlayer(), mobile, context.getType()));
        return StepResult.success();
    }
}
