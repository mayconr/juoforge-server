package com.github.mayconr.juoserver.game.combat;

import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.WarModeType;
import com.github.mayconr.juoserver.game.world.WorldModule;
import com.github.mayconr.juoserver.network.packet.AttackRequest;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CombatModule implements WorldModule, CombatCommands {

    private final CombatHandler combatHandler;
    private final VitalsHandler vitalsHandler;

    @Override
    public void update(double delta) {

    }

    @Override
    public void toggleWarMode(UOPlayer player, WarModeType type) {
        combatHandler.toggleWarMode(player, type);
    }

    @Override
    public void attack(UOPlayer player, AttackRequest request) {
        combatHandler.attack(player, request);
    }

    @Override
    public void regen(UOMobile mobile, double interval) {
        vitalsHandler.regen(mobile, interval);
    }
}
