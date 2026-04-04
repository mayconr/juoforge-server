package com.github.mayconr.juoserver.game.combat;

import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.game.model.CharacterStatus;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.WarModeType;
import com.github.mayconr.juoserver.game.model.event.MobileStatusChanged;
import com.github.mayconr.juoserver.game.model.event.PlayerStartAttack;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import com.github.mayconr.juoserver.network.packet.AttackRequest;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CombatHandler {

    private final EventBus eventBus;
    private final CombatSystem combatSystem;
    private final RealmStorage storage;

    public void toggleWarMode(UOPlayer player, WarModeType type) {
        var oldStatus = player.getStatus();

        if (WarModeType.NORMAL.equals(type)) {
            player.setStatus(CharacterStatus.NORMAL);
            if (combatSystem.isAttacking(player.getSerialId())) {
                combatSystem.cancelAttack(player.getSerialId());
                //outbound.write(new AttackCharacter(0));
            }
        } else if (WarModeType.FIGHTING.equals(type)) {
            player.setStatus(CharacterStatus.WAR_MODE);
        }

        eventBus.publish(new MobileStatusChanged(player, player.getStatus(), oldStatus));
    }

    public void attack(UOPlayer player, AttackRequest request) {
        final var opponent = storage.getMobile(request.getOpponentSerialId())
                .orElseThrow(() -> new IllegalArgumentException("Opponent not found"));
        combatSystem.requestAttack(player.getSerialId(), request.getOpponentSerialId());
        // TODO validate (range, LOS, cooldown, warmode, stamina, flags)
        eventBus.publish(new PlayerStartAttack(player, opponent));
    }
}
