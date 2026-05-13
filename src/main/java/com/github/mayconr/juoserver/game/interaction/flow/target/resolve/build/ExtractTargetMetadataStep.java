package com.github.mayconr.juoserver.game.interaction.flow.target.resolve.build;

import com.github.mayconr.juoserver.game.model.TargetResult;
import com.github.mayconr.juoserver.game.interaction.flow.target.resolve.ResolveTargetContext;
import com.github.mayconr.juoserver.game.model.PointInTheWorld;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

import java.util.function.Consumer;

public class ExtractTargetMetadataStep extends AbstractFlowStep<ResolveTargetContext> {
    public ExtractTargetMetadataStep() {
        super("ExtractTargetMetadata");
    }

    @Override
    public StepResult execute(ResolveTargetContext ctx) {
        var target = ctx.getTarget();
        var attrs = ctx.getPlayer().runtimeAttributes();

        var consumer = (Consumer<TargetResult>)
                attrs.remove("TARGET_" + target.getCursorId());

        if (consumer == null) {
            return StepResult.failure("No consumers found");
        }

        ctx.setCallback(consumer);
        ctx.setTargetLocation(new PointInTheWorld(target.getX(), target.getY(), target.getZ()));
        ctx.setCursorTarget(target.getTarget());
        ctx.setSerialId(target.getClickedSerialId());
        ctx.setModelId(target.getModelId());

        return StepResult.success();
    }
}
