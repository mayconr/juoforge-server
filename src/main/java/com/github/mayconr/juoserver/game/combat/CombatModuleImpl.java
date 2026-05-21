package com.github.mayconr.juoserver.game.combat;

import com.github.mayconr.juoserver.game.combat.commands.AttackCommand;
import com.github.mayconr.juoserver.game.combat.commands.CombatCommand;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.WarModeType;
import com.github.mayconr.juoserver.network.packet.AttackRequest;
import lombok.RequiredArgsConstructor;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@RequiredArgsConstructor
public class CombatModuleImpl implements CombatModule {

    private static final double UPDATE_DELAY_SECONDS = 0.5;

    private final CombatHandler combatHandler;
    private final VitalsHandler vitalsHandler;

    private final Queue<CombatCommand> commandQueue = new ConcurrentLinkedQueue<>();
    private double updateAccumulator;

    @Override
    public void update(double delta) {
        updateAccumulator += delta;
        if (updateAccumulator < UPDATE_DELAY_SECONDS) {
            return;
        }
        updateAccumulator = 0;

        CombatCommand command;
        while ((command = commandQueue.poll()) != null) {
            //commandDispatcher.dispatch(command);
            System.out.println(command);
        }

        //for (CombatState state : activeCombatRegistry.activeStates()) {
            //attackSwingFlow.execute(new AttackSwingContext(state, delta));
        //}
    }

    @Override
    public void toggleWarMode(UOPlayer player, WarModeType type) {
        combatHandler.toggleWarMode(player, type);
    }

    @Override
    public void attack(UOPlayer player, AttackRequest request) {
        commandQueue.add(new AttackCommand());
        System.out.println("foi");
        //combatHandler.attack(player, request);
    }

    @Override
    public void regen(UOMobile mobile, double interval) {
        vitalsHandler.regen(mobile, interval);
    }
}
