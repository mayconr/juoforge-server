package com.github.mayconr.juoserver.game.combat;

import com.github.mayconr.juoserver.game.model.CharacterStatus;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.WarModeType;
import com.github.mayconr.juoserver.game.model.event.MobileStatusChanged;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CombatHandler {

    private final EventBus eventBus;

    public void toggleWarMode(UOPlayer player, WarModeType type) {
        var oldStatus = player.getStatus();

        if (WarModeType.NORMAL.equals(type)) {
            player.setStatus(CharacterStatus.NORMAL);
        } else if (WarModeType.FIGHTING.equals(type)) {
            player.setStatus(CharacterStatus.WAR_MODE);
        }

        eventBus.publish(new MobileStatusChanged(player, player.getStatus(), oldStatus));
    }

}
