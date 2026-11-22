package com.github.mayconr.juoserver.game.ui.flow.doubleclick;

import com.github.mayconr.juoserver.game.ui.WorldUI;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public final class OpenClickedPaperdollStep extends AbstractFlowStep<DoubleClickContext> {
    private final WorldUI ui;

    public OpenClickedPaperdollStep(WorldUI ui) {
        super("open-clicked-paperdoll");
        this.ui = ui;
    }

    @Override
    public StepResult execute(DoubleClickContext context) {
        ui.openPaperdoll(context.getPlayer(), context.paperdollSerial());
        return StepResult.success();
    }
}
