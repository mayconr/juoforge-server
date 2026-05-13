package com.github.mayconr.juoserver.game.interaction;

import com.github.mayconr.juoserver.game.interaction.action.ActionHandler;
import com.github.mayconr.juoserver.game.interaction.animation.AnimationHandler;
import com.github.mayconr.juoserver.game.interaction.flow.target.resolve.ResolveTargetContext;
import com.github.mayconr.juoserver.game.interaction.flow.target.send.SendTargetContext;
import com.github.mayconr.juoserver.game.interaction.speech.SpeechHandler;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.world.context.ModuleContext;
import com.github.mayconr.juoserver.network.packet.ActionRequest;
import com.github.mayconr.juoserver.network.packet.Target;
import com.github.mayconr.juoserver.network.packet.UnicodeSpeachRequest;
import lombok.RequiredArgsConstructor;

import java.util.function.Consumer;

@RequiredArgsConstructor
public class InteractionModuleImpl implements InteractionModule {

    private final ActionHandler actionHandler;
    private final AnimationHandler animationHandler;
    private final SpeechHandler speechHandler;

    private ModuleContext.FlowFacade flows;

    @Override
    public void initialize(ModuleContext context) {
        this.flows = context.flows();
    }

    @Override
    public void sendTarget(UOPlayer player, CursorType type, Consumer<TargetResult> consumer) {
        flows.execute(new SendTargetContext(player, type, consumer));
    }

    @Override
    public void resolveTarget(UOPlayer player, Target target) {
        flows.execute(ResolveTargetContext.of(player, target, true));
    }

    @Override
    public void handleAction(UOPlayer player, ActionRequest request) {
        actionHandler.handleAction(player, request);
    }

    @Override
    public void sendAnimation(UOMobile mobile, AnimationOptions options) {
        animationHandler.sendAnimation(mobile, options);
    }

    @Override
    public void speech(UOPlayer player, UnicodeSpeachRequest request) {
        speechHandler.speech(player, request);
    }
}
