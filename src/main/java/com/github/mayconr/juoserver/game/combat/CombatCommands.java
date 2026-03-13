package com.github.mayconr.juoserver.game.combat;

import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.WarModeType;
import com.github.mayconr.juoserver.network.packet.AttackRequest;

public interface CombatCommands {
    void toggleWarMode(UOPlayer player, WarModeType type);

    void attack(UOPlayer player, AttackRequest request);

    void regen(UOMobile mobile, double interval);
}
