package com.github.mayconr.juoserver.game.vitals.flow.regen;

import com.github.mayconr.juoserver.game.model.event.VitalsChanged;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.ui.WorldUI;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public final class NotifyRegenStep extends AbstractFlowStep<RegenContext> {
    private final EventBus eventBus;
    private final WorldUI ui;

    public NotifyRegenStep(EventBus eventBus, WorldUI ui) {
        super("notify-regen");
        this.eventBus = eventBus;
        this.ui = ui;
    }

    @Override
    public StepResult execute(RegenContext context) {
        if (context.isChanged()) {
            eventBus.publish(new VitalsChanged(context.getMobile()));
            if (context.getMobile() instanceof UOPlayer player) {
                ui.sendStatusGump(player, player.getSerialId());
            }
        }
        return StepResult.success();
    }
}
