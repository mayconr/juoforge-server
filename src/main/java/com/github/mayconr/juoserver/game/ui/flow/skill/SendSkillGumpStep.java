package com.github.mayconr.juoserver.game.ui.flow.skill;

import com.github.mayconr.juoserver.game.model.event.SkillGumpRequested;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;

public final class SendSkillGumpStep extends AbstractFlowStep<SkillGumpContext> {
    private final EventBus events;
    private final RealmStorage storage;

    public SendSkillGumpStep(EventBus events, RealmStorage storage) {
        super("send-skill-gump");
        this.events = events;
        this.storage = storage;
    }

    @Override
    public StepResult execute(SkillGumpContext context) {
        var mobile = storage.getMobile(context.getMobileSerial()).orElse(null);
        if (mobile == null) {
            return StepResult.failure("MOBILE_NOT_FOUND", "Mobile not found " + context.getMobileSerial());
        }
        events.publish(new SkillGumpRequested(context.getPlayer(), mobile, context.getType()));
        return StepResult.success();
    }
}
