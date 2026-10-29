package com.github.mayconr.juoserver.game.skill;

import com.github.mayconr.juoserver.game.model.SkillGainContext;
import com.github.mayconr.juoserver.game.model.SkillValue;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.skill.flow.use.UseSkillContext;
import com.github.mayconr.juoserver.game.world.context.ModuleContext;
import lombok.RequiredArgsConstructor;
import com.github.mayconr.juoserver.game.skill.flow.lock.SendSkillsLockContext;
import com.github.mayconr.juoserver.game.world.ModuleOperationException;

import java.util.Collection;
import com.github.mayconr.juoserver.game.skill.flow.set.SetSkillContext;

@lombok.extern.slf4j.Slf4j
@RequiredArgsConstructor
public class SkillModuleImpl implements SkillModule {

    private final SkillSystem skillSystem;
    private final com.github.mayconr.juoserver.game.messaging.WorldMessage messages;

    private ModuleContext.FlowFacade flows;

    private final java.util.concurrent.ConcurrentMap<Integer, SkillAction> actions =
            new java.util.concurrent.ConcurrentHashMap<>();

    @Override
    public boolean startAction(UOPlayer player, int swings, double delaySeconds,
                               SkillActionCallback swing, SkillActionCallback onComplete) {
        java.util.Objects.requireNonNull(player, "Player is required");
        java.util.Objects.requireNonNull(swing, "Swing is required");
        java.util.Objects.requireNonNull(onComplete, "Completion is required");
        if (swings <= 0) throw new IllegalArgumentException("swings must be positive");
        if (!Double.isFinite(delaySeconds) || delaySeconds < 0) {
            throw new IllegalArgumentException("delaySeconds must be finite and non-negative");
        }
        if (actions.putIfAbsent(player.getSerialId(),
                new SkillAction(swings, delaySeconds, swing, onComplete)) != null) {
            sendBusyMessage(player);
            return false;
        }
        return true;
    }

    @Override
    public boolean hasActiveAction(UOPlayer player) {
        return actions.containsKey(java.util.Objects.requireNonNull(player).getSerialId());
    }

    @Override
    public void update(double delta) {
        if (!Double.isFinite(delta) || delta < 0) {
            throw new IllegalArgumentException("delta must be finite and non-negative");
        }
        // Snapshot keeps actions created by callbacks for the next update.
        for (var entry : java.util.List.copyOf(actions.entrySet())) {
            var action = entry.getValue();
            action.remainingSeconds -= delta;
            if (action.remainingSeconds > 1e-9) continue;
            try {
                action.swing.execute(++action.swingsExecuted);
                if (--action.swingsLeft == 0) {
                    actions.remove(entry.getKey(), action);
                    action.onComplete.execute(action.swingsExecuted);
                } else {
                    action.remainingSeconds = action.delaySeconds;
                }
            } catch (Exception e) {
                actions.remove(entry.getKey(), action);
                log.error("Skill action failed for player {}", entry.getKey(), e);
            }
        }
    }

    private static final class SkillAction {
        private int swingsLeft;
        private int swingsExecuted;
        private double remainingSeconds;
        private final double delaySeconds;
        private final SkillActionCallback swing;
        private final SkillActionCallback onComplete;

        private SkillAction(int swings, double delaySeconds, SkillActionCallback swing, SkillActionCallback onComplete) {
            this.swingsLeft = swings;
            this.delaySeconds = delaySeconds;
            this.swing = swing;
            this.onComplete = onComplete;
        }
    }

    @Override
    public void initialize(ModuleContext context) {
        this.flows = context.flows();
    }

    @Override
    public void sendSkillsLock(UOPlayer player, Collection<SkillValue> skills) {
        var result = flows.execute(new SendSkillsLockContext(player, skills));
        if (result.flowFailed()) {
            throw ModuleOperationException.fromFailure("sendSkillsLock", result, null);
        }
    }

    @Override
    public void setSkill(UOPlayer player, int skillId, double value) {
        java.util.Objects.requireNonNull(player, "Player is required");
        if (skillId < 0 || skillId > 0xFFFF) {
            throw new IllegalArgumentException("Skill ID must fit an unsigned short");
        }
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException("Skill value must be finite");
        }
        var result = flows.execute(new SetSkillContext(player, skillId, value));
        if (result.flowFailed()) {
            throw ModuleOperationException.fromFailure("setSkill", result, null);
        }
    }

    @Override
    public void useSkill(UOPlayer player, int skillId) {
        if (hasActiveAction(player)) {
            sendBusyMessage(player);
            return;
        }
        flows.execute(new UseSkillContext(player, skillId));
    }

    private void sendBusyMessage(UOPlayer player) {
        messages.send(player, com.github.mayconr.juoserver.game.model.event.message.MessageContent
                .localized("skill.action.already_active"));
    }

    @Override
    public void tryGain(UOMobile mobile, int skillId, double difficulty, SkillGainContext context) {
        skillSystem.tryGain(mobile, skillId, difficulty, context);
    }
}
