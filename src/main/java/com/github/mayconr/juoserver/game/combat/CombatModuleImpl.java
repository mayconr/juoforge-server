package com.github.mayconr.juoserver.game.combat;

import com.github.mayconr.juoserver.game.combat.commands.AttackCommand;
import com.github.mayconr.juoserver.game.combat.commands.CancelAttackCommand;
import com.github.mayconr.juoserver.game.combat.commands.CombatCommand;
import com.github.mayconr.juoserver.game.combat.flow.execution.CombatExecutionContext;
import com.github.mayconr.juoserver.game.combat.flow.preparation.CombatPreparationContext;
import com.github.mayconr.juoserver.game.combat.flow.preparation.CombatPreparationContext.CombatOrigin;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.WarModeType;
import com.github.mayconr.juoserver.game.world.context.ModuleContext;
import com.github.mayconr.juoserver.network.packet.AttackRequest;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@RequiredArgsConstructor
public class CombatModuleImpl implements CombatModule {

    private static final double UPDATE_DELAY_SECONDS = 1;

    private final CombatHandler combatHandler;
    private final VitalsHandler vitalsHandler;

    private final Queue<CombatCommand> commandQueue = new ConcurrentLinkedQueue<>();
    private final CombatSessionRegistry registry = new CombatSessionRegistryImpl();

    private ModuleContext.FlowFacade flows;
    private double updateAccumulator;

    @Override
    public void initialize(ModuleContext context) {
        this.flows = context.flows();
    }

    @Override
    public void update(double delta) {
        updateAccumulator += delta;
        if (updateAccumulator < UPDATE_DELAY_SECONDS) {
            return;
        }
        updateAccumulator = 0;

        processCombat();
    }

    private void processCombat() {
        CombatCommand command;
        while ((command = commandQueue.poll()) != null) {
            switch (command) {
                case AttackCommand attack -> {
                    var context = CombatPreparationContext.of(attack.attacker(), attack.targetSerial(), attack.origin());
                    flows.execute(context);

                    var session = context.getSession();
                    if (session != null) {
                        registry.register(session);
                    }
                }

                case CancelAttackCommand cancel -> {
                    registry.unregister(cancel.mobile());
                }
            }
        }

        final var inactiveSessions = new ArrayList<CombatSession>();
        for (CombatSession session : registry.getSessions()) {
            if (!session.isActive()) {
                inactiveSessions.add(session);
                continue;
            }
            flows.execute(new CombatExecutionContext(session));
        }

        // Remove inactive sessions
        for (CombatSession session : inactiveSessions) {
            registry.unregister(session);
        }
    }

    @Override
    public void toggleWarMode(UOPlayer player, WarModeType type) {
        combatHandler.toggleWarMode(player, type);
        if (type == WarModeType.NORMAL) {
            commandQueue.add(new CancelAttackCommand(player));
        }
    }

    @Override
    public void requestAttack(UOPlayer player, AttackRequest request) {
        commandQueue.add(new AttackCommand(player, request.getOpponentSerialId(), CombatOrigin.ofRequest()));
    }

    @Override
    public void requestSpellCast(UOPlayer player, UOMobile target) {
        commandQueue.add(new AttackCommand(player, target.getSerialId(), CombatOrigin.ofSpell()));
    }

    @Override
    public void regen(UOMobile mobile, double interval) {
        vitalsHandler.regen(mobile, interval);
    }
}
