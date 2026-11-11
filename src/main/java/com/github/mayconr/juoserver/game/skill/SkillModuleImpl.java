package com.github.mayconr.juoserver.game.skill;

import com.github.mayconr.juoserver.game.messaging.WorldMessage;
import com.github.mayconr.juoserver.game.model.SkillGainContext;
import com.github.mayconr.juoserver.game.model.SkillValue;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.skill.flow.start.StartSkillActionContext;
import com.github.mayconr.juoserver.game.skill.flow.stop.StopSkillActionContext;
import com.github.mayconr.juoserver.game.skill.flow.update.UpdateSkillActionsContext;
import com.github.mayconr.juoserver.game.skill.flow.use.UseSkillContext;
import com.github.mayconr.juoserver.game.skill.flow.gain.TryGainSkillContext;
import com.github.mayconr.juoserver.game.world.context.ModuleContext;
import lombok.RequiredArgsConstructor;
import com.github.mayconr.juoserver.game.skill.flow.lock.SendSkillsLockContext;
import com.github.mayconr.juoserver.game.world.ModuleOperationException;

import java.util.Collection;
import java.util.Map;
import com.github.mayconr.juoserver.game.model.event.message.MessageContent;
import java.util.concurrent.ConcurrentHashMap;

import com.github.mayconr.juoserver.game.skill.flow.set.SetSkillContext;
import com.github.mayconr.juoserver.game.skill.flow.swings.SetSkillSwingCountContext;

import static java.util.Objects.requireNonNull;

@RequiredArgsConstructor
public class SkillModuleImpl implements SkillModule {

    private final WorldMessage messages;

    private ModuleContext.FlowFacade flows;

    private final ConcurrentHashMap<Integer, SkillAction> actions = new ConcurrentHashMap<>();

    @Override
    public void setSwingCount(UOPlayer player, int swingCount) {
        requireNonNull(player, "Player is required");
        var result = flows.execute(new SetSkillSwingCountContext(player, swingCount));
        if (result.flowFailed()) {
            throw ModuleOperationException.fromFailure("setSwingCount", result, null);
        }
    }

    @Override
    public boolean startSkillAction(UOPlayer player, SkillActionOptions options) {
        var context = new StartSkillActionContext(player, options);
        var result = flows.execute(context);
        if (result.flowFailed()) {
            throw ModuleOperationException.fromFailure("startSkillAction", result, null);
        }
        return context.result();
    }

    @Override
    public boolean stopSkillAction(UOPlayer player) {
        requireNonNull(player, "Player is required");
        var context = new StopSkillActionContext(player);
        var result = flows.execute(context);
        if (result.flowFailed()) {
            throw ModuleOperationException.fromFailure("stopSkillAction", result, null);
        }
        return context.result();
    }

    @Override
    public boolean reserveSkillAction(UOPlayer player, SkillAction action) {
        return actions.putIfAbsent(
                requireNonNull(player, "Player is required").getSerialId(),
                requireNonNull(action, "Skill action is required")
        ) == null;
    }

    @Override
    public boolean hasActiveAction(UOPlayer player) {
        return actions.containsKey(requireNonNull(player).getSerialId());
    }

    @Override
    public Map<Integer, SkillAction> getSkillActions() {
        return Map.copyOf(actions);
    }

    @Override
    public void releaseSkillAction(int playerSerial, SkillAction action) {
        actions.remove(playerSerial, action);
    }

    @Override
    public void update(double delta) {
        var result = flows.execute(new UpdateSkillActionsContext(delta));
        if (result.flowFailed()) {
            throw ModuleOperationException.fromFailure("updateSkillActions", result, null);
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
        requireNonNull(player, "Player is required");
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
        messages.send(player, MessageContent
                .localized("skill.action.already_active"));
    }

    @Override
    public void tryGain(UOMobile mobile, int skillId, double trainingIntensity, SkillGainContext context) {
        var result = flows.execute(new TryGainSkillContext(
                mobile, skillId, trainingIntensity, context));
        if (result.flowFailed()) {
            throw ModuleOperationException.fromFailure("tryGain", result, null);
        }
    }
}
