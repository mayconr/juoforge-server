package com.github.mayconr.juoserver.game.interaction.flow.target.resolve.build;

import com.github.mayconr.juoserver.game.model.ItemTargetResult;
import com.github.mayconr.juoserver.game.model.MobileTargetResult;
import com.github.mayconr.juoserver.game.model.StaticTargetResult;
import com.github.mayconr.juoserver.game.model.TargetResult;
import com.github.mayconr.juoserver.game.interaction.flow.target.resolve.ResolveTargetContext;
import com.github.mayconr.juoserver.game.model.CursorTarget;
import com.github.mayconr.juoserver.infrastructure.datafile.UOFileReader;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public class BuildTargetResultStep extends AbstractFlowStep<ResolveTargetContext> {

    private final UOFileReader fileReader;

    public BuildTargetResultStep(UOFileReader fileReader) {
        super("ResolveTargetResult");
        this.fileReader = fileReader;
    }

    @Override
    public StepResult execute(ResolveTargetContext ctx) {
        TargetResult result;

        if (CursorTarget.LOCATION.equals(ctx.getCursorTarget())) {
            final var landTile = fileReader.getLandTile(ctx.getTargetLocation());
            final var statics = fileReader.getStatics(ctx.getTargetLocation());

            result = new StaticTargetResult(
                    ctx.getPlayer(),
                    ctx.getTargetLocation(),
                    landTile,
                    statics
            );
        } else if (ctx.getMobile() != null) {
            result = new MobileTargetResult(
                    ctx.getPlayer(),
                    ctx.getTargetLocation(),
                    ctx.getMobile()
            );
        } else {
            result = new ItemTargetResult(
                    ctx.getPlayer(),
                    ctx.getTargetLocation(),
                    ctx.getItem()
            );
        }

        ctx.setTargetResult(result);
        return StepResult.success();
    }
}
