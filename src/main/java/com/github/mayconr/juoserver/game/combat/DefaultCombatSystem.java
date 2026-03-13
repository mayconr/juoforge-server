package com.github.mayconr.juoserver.game.combat;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

import com.github.mayconr.juoserver.infrastructure.gameloop.IntervalGameTask;
import com.github.mayconr.juoserver.game.model.AnimationDirection;
import com.github.mayconr.juoserver.game.model.AnimationRepeat;
import com.github.mayconr.juoserver.game.model.AnimationType;
import com.github.mayconr.juoserver.network.packet.CharacterAnimation;

import io.netty.channel.group.ChannelGroup;

public class DefaultCombatSystem extends IntervalGameTask implements CombatSystem {

    private final Map<Integer, CombatState> combatStateMap = new HashMap<>();
    private final Queue<CombatCommand> commandQueue = new ConcurrentLinkedQueue<>();
    private final ConcurrentMap<Integer, Boolean> attackingIndex = new ConcurrentHashMap<>();
    private final ChannelGroup channelGroup;

    public DefaultCombatSystem(ChannelGroup channelGroup) {
        super(10);
        this.channelGroup = channelGroup;
    }

    @Override
    public void requestAttack(int attackerId, int targetId) {
        commandQueue.add(new RequestAttack(attackerId, targetId));
    }

    @Override
    public void cancelAttack(int attackerId) {
        commandQueue.add(new CancelAttack(attackerId));
    }

    @Override
    public boolean isAttacking(int attackerId) {
        return attackingIndex.containsKey(attackerId);
    }

    @Override
    public void execute(double delta) {
        CombatCommand cmd;
        while ((cmd = commandQueue.poll()) != null) {
            cmd.apply(combatStateMap, attackingIndex);
        }

        final var it = combatStateMap.entrySet().iterator();

        while (it.hasNext()) {
            final var entry = it.next();
            final var state = entry.getValue();

            channelGroup.writeAndFlush(
                    new CharacterAnimation(
                            state.getAttackerId(),
                            AnimationRepeat.ONCE,
                            AnimationType.ATTACK_WITH_SWORD_OVER_AND_SIDE,
                            100,
                            AnimationDirection.FORWARD));

            if (!state.isAutoSwing()) {
                it.remove();
                attackingIndex.remove(state.getAttackerId());
            }
        }
    }

    public interface CombatCommand {
        void apply(
                Map<Integer, CombatState> combatStateMap,
                ConcurrentMap<Integer, Boolean> attackingIndex);
    }

    private record RequestAttack(int attackerId, int targetId) implements CombatCommand {
        @Override
        public void apply(
                Map<Integer, CombatState> combatStateMap,
                ConcurrentMap<Integer, Boolean> attackingIndex) {
            final var state =
                    combatStateMap.computeIfAbsent(attackerId, id -> new CombatState(id, targetId));
            state.setSwing(1);
            state.setNextImpactAt(
                    500); // windup, time until the next attack. compute based on strength dex,
            // weapon
            state.setAutoSwing(true);
            attackingIndex.put(attackerId, Boolean.TRUE);
        }
    }

    private record CancelAttack(int attackerId) implements CombatCommand {
        @Override
        public void apply(
                Map<Integer, CombatState> combatStateMap,
                ConcurrentMap<Integer, Boolean> attackingIndex) {
            final var state = combatStateMap.get(attackerId);
            if (state != null) {
                state.setAutoSwing(false);
            }
        }
    }
}
