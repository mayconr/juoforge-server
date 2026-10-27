package com.github.mayconr.juoserver.game.ui.flow.singleclick;

import com.github.mayconr.juoserver.game.model.UOObject;
import com.github.mayconr.juoserver.game.model.event.ObjectLabelRequested;
import com.github.mayconr.juoserver.game.world.SerialGenerator;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;

import java.util.Optional;

public final class RequestObjectLabelStep extends AbstractFlowStep<SingleClickContext> {
    private final RealmStorage storage;
    private final EventBus events;

    public RequestObjectLabelStep(RealmStorage storage, EventBus events) {
        super("request-object-label");
        this.storage = storage;
        this.events = events;
    }

    @Override
    public StepResult execute(SingleClickContext context) {
        int serial = context.getRequest().getSerialId();
        Optional<? extends UOObject<?>> object = Optional.empty();
        if (SerialGenerator.isMobile(serial)) {
            object = storage.getMobile(serial);
        } else if (SerialGenerator.isItem(serial)) {
            object = storage.getItem(serial);
        }
        object.ifPresent(value -> events.publish(new ObjectLabelRequested(context.getPlayer(), value)));
        return StepResult.success();
    }
}
