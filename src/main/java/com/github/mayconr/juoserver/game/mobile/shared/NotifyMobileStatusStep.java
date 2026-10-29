package com.github.mayconr.juoserver.game.mobile.shared;

import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.ui.WorldUI;
import com.github.mayconr.juoserver.infrastructure.flow.FlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class NotifyMobileStatusStep<T extends MobileStatusContext> implements FlowStep<T> {
    private final WorldUI ui;

    @Override
    public String name() {
        return "notify-mobile-status";
    }

    @Override
    public StepResult execute(T context) {
        if (context.getMobile() instanceof UOPlayer player) {
            ui.sendStatusGump(player, player.getSerialId());
        }
        return StepResult.success();
    }
}
