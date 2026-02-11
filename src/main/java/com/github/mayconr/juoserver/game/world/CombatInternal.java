package com.github.mayconr.juoserver.game.world;

import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.WarModeType;
import com.github.mayconr.juoserver.network.packet.AttackRequest;

public interface CombatInternal {

    void toggleWarMode(UOPlayer player, WarModeType type);

    void attack(UOPlayer player, AttackRequest request);
}
