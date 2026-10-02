package com.github.mayconr.juoserver.game.combat;

import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.world.WorldModule;
import com.github.mayconr.juoserver.network.packet.AttackRequest;

public interface CombatModule extends WorldCombat, WorldModule {
    default void requestAttack(UOPlayer player, AttackRequest request) {
        requestAttack(player, request.getOpponentSerialId());
    }

    void requestSpellCast(UOPlayer player, UOMobile target);

}
