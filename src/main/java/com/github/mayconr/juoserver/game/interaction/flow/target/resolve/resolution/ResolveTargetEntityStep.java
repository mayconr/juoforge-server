package com.github.mayconr.juoserver.game.interaction.flow.target.resolve.resolution;

import com.github.mayconr.juoserver.game.interaction.flow.target.resolve.ResolveTargetContext;
import com.github.mayconr.juoserver.game.model.CursorTarget;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;

public class ResolveTargetEntityStep extends AbstractFlowStep<ResolveTargetContext> {

    private final RealmStorage storage;

    public ResolveTargetEntityStep(RealmStorage storage) {
        super("BuildTargetResult");
        this.storage = storage;
    }

    @Override
    public StepResult execute(ResolveTargetContext context) {
        if (CursorTarget.LOCATION.equals(context.getCursorTarget())) {
            return StepResult.success();
        }

        int serial = context.getSerialId();

        if (UOMobile.isMobile(serial)) {
            return storage.getMobile(serial)
                    .map(m -> {
                        context.setMobile(m);
                        return StepResult.success();
                    })
                    .orElse(StepResult.failure("Mobile not found"));
        }

        if (UOItem.isItem(serial)) {
            return storage.getItem(serial)
                    .map(i -> {
                        context.setItem(i);
                        return StepResult.success();
                    })
                    .orElse(StepResult.failure("Item not found"));
        }

        return StepResult.failure("Unknown cursor type");
    }
}
