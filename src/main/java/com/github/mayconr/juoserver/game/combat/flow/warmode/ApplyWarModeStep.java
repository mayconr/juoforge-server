package com.github.mayconr.juoserver.game.combat.flow.warmode;

import com.github.mayconr.juoserver.game.model.CharacterStatus;
import com.github.mayconr.juoserver.game.model.WarModeType;
import com.github.mayconr.juoserver.game.model.event.MobileStatusChanged;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public class ApplyWarModeStep extends AbstractFlowStep<ToggleWarModeContext> {
    private final EventBus eventBus;

    public ApplyWarModeStep(EventBus eventBus) {
        super("ApplyWarModeStep");
        this.eventBus = eventBus;
    }

    @Override
    public StepResult execute(ToggleWarModeContext context) {
        var player = context.getPlayer();
        var oldStatus = player.getStatus();
        var type = context.getType();

        if (type == WarModeType.NORMAL) {
            player.setStatus(CharacterStatus.NORMAL);
        } else if (type == WarModeType.FIGHTING) {
            player.setStatus(CharacterStatus.WAR_MODE);
        }

        eventBus.publish(new MobileStatusChanged(player, player.getStatus(), oldStatus));
        return StepResult.success();
    }
}
