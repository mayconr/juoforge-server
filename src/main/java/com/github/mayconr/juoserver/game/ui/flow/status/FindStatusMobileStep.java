package com.github.mayconr.juoserver.game.ui.flow.status;

import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;

public final class FindStatusMobileStep extends AbstractFlowStep<StatusGumpContext> {
    private final RealmStorage storage;

    public FindStatusMobileStep(RealmStorage storage) {
        super("find-status-mobile");
        this.storage = storage;
    }

    @Override
    public StepResult execute(StatusGumpContext context) {
        var mobile = storage.getMobile(context.getMobileSerial()).orElse(null);
        if (mobile == null) {
            return StepResult.failure("MOBILE_NOT_FOUND", "Mobile not found " + context.getMobileSerial());
        }
        context.setMobile(mobile);
        return StepResult.success();
    }
}
