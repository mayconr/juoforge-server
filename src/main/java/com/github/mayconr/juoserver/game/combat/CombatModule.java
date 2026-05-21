package com.github.mayconr.juoserver.game.combat;

import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.WarModeType;
import com.github.mayconr.juoserver.game.world.WorldModule;
import com.github.mayconr.juoserver.network.packet.AttackRequest;

public interface CombatModule extends WorldModule {
    void toggleWarMode(UOPlayer player, WarModeType type);

    void attack(UOPlayer player, AttackRequest request);

    void regen(UOMobile mobile, double interval);
}
