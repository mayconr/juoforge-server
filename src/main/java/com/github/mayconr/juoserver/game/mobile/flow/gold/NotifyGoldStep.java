package com.github.mayconr.juoserver.game.mobile.flow.gold;

import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.event.MobileGoldChanged;
import com.github.mayconr.juoserver.game.ui.WorldUI;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public final class NotifyGoldStep extends AbstractFlowStep<RecalculateGoldContext> {
    private final EventBus events;
    private final WorldUI ui;

    public NotifyGoldStep(EventBus events, WorldUI ui) {
        super("notify-mobile-gold");
        this.events = events;
        this.ui = ui;
    }

    @Override
    public StepResult execute(RecalculateGoldContext context) {
        var mobile = context.getMobile();
        events.publish(new MobileGoldChanged(mobile, context.getOldBalance(), mobile.getGold()));
        if (mobile instanceof UOPlayer player) {
            ui.sendStatusGump(player, player.getSerialId());
        }
        return StepResult.success();
    }
}
