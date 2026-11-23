package com.github.mayconr.juoserver.game.combat;

import com.github.mayconr.juoserver.game.combat.commands.AttackCommand;
import com.github.mayconr.juoserver.game.combat.commands.CancelAttackCommand;
import com.github.mayconr.juoserver.game.combat.commands.CombatCommand;
import com.github.mayconr.juoserver.game.combat.flow.execution.CombatExecutionContext;
import com.github.mayconr.juoserver.game.combat.flow.preparation.CombatPreparationContext;
import com.github.mayconr.juoserver.game.combat.flow.preparation.CombatPreparationContext.CombatOrigin;
import com.github.mayconr.juoserver.game.combat.flow.warmode.ToggleWarModeContext;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.WarModeType;
import com.github.mayconr.juoserver.game.model.event.MobileMoved;
import com.github.mayconr.juoserver.game.world.context.ModuleContext;

import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentHashMap;

public class CombatModuleImpl implements CombatModule {

    private static final double SWING_INTERVAL_SECONDS = 1;

    private final Queue<CombatCommand> commandQueue = new ConcurrentLinkedQueue<>();
    private final Map<Integer, AttackCommand> pendingAttacks = new ConcurrentHashMap<>();
    private final Map<Integer, Double> nextSwingAt = new ConcurrentHashMap<>();
    private final CombatSessionRegistry registry = new CombatSessionRegistryImpl();

    private ModuleContext.FlowFacade flows;
    private volatile double elapsedSeconds;

    @Override
    public void initialize(ModuleContext context) {
        this.flows = context.flows();
    }

    @Override
    public void update(double delta) {
        elapsedSeconds += delta;
        nextSwingAt.values().removeIf(readyAt -> readyAt <= elapsedSeconds);
        processCommands();
        for (CombatSession session : registry.getSessions()) {
            if (session.isActive()) {
                trySwing(session, false);
            }
            if (!session.isActive()) {
                registry.unregister(session);
            }
        }
    }

    public void onMobileMoved(MobileMoved event) {
        if (event.teleport()) {
            return;
        }
        int serial = event.mobile().getSerialId();
        for (CombatSession session : registry.getSessions()) {
            if (session.getTrigger() instanceof CombatSession.PhysicalTrigger
                    && (session.getAttacker().getSerialId() == serial
                    || session.getTarget().getSerialId() == serial)) {
                trySwing(session, true);
            }
        }
    }

    private void processCommands() {
        CombatCommand command;
        while ((command = commandQueue.poll()) != null) {
            switch (command) {
                case AttackCommand attack -> {
                    if (attack.origin() instanceof CombatPreparationContext.RequestOrigin) {
                        if (!claimPendingAttack(attack)) continue;
                        if (isAttackAlreadyActive(attack)) continue;
                    }
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

    }

    private void trySwing(CombatSession session, boolean fromMovement) {
        int serial = session.getAttacker().getSerialId();
        if (session.isActive() && !nextSwingAt.containsKey(serial)) {
            flows.execute(new CombatExecutionContext(session, fromMovement, () -> reserveSwing(session)));
        }
    }

    private boolean reserveSwing(CombatSession session) {
        return session.isActive() && nextSwingAt.putIfAbsent(
                session.getAttacker().getSerialId(), elapsedSeconds + SWING_INTERVAL_SECONDS) == null;
    }

    @Override
    public void toggleWarMode(UOPlayer player, WarModeType type) {
        flows.execute(new ToggleWarModeContext(player, type));
        if (type == WarModeType.NORMAL) {
            requestCancelAttack(player);
        }
    }

    @Override
    public synchronized void requestAttack(UOMobile attacker, int targetSerial) {
        var pending = pendingAttacks.get(attacker.getSerialId());
        if (pending != null && pending.targetSerial() == targetSerial) return;
        var command = new AttackCommand(attacker, targetSerial, CombatOrigin.ofRequest());
        pendingAttacks.put(attacker.getSerialId(), command);
        commandQueue.add(command);
    }

    private synchronized boolean claimPendingAttack(AttackCommand command) {
        int serial = command.attacker().getSerialId();
        if (pendingAttacks.get(serial) != command) return false;
        pendingAttacks.remove(serial);
        return true;
    }

    private boolean isAttackAlreadyActive(AttackCommand command) {
        var session = registry.getByPlayer(command.attacker());
        return session != null && session.isActive()
                && session.getTarget().getSerialId() == command.targetSerial();
    }

    @Override
    public synchronized void requestCancelAttack(UOMobile mobile) {
        Objects.requireNonNull(mobile);
        pendingAttacks.remove(mobile.getSerialId());
        var session = registry.getByPlayer(mobile);
        if (session != null) session.close();
        commandQueue.add(new CancelAttackCommand(mobile));
    }

    @Override
    public void requestSpellCast(UOPlayer player, UOMobile target) {
        commandQueue.add(new AttackCommand(player, target.getSerialId(), CombatOrigin.ofSpell()));
    }

}
