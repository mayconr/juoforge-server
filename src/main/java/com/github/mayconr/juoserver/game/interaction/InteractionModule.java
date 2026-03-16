package com.github.mayconr.juoserver.game.interaction;

import com.github.mayconr.juoserver.game.interaction.action.ActionHandler;
import com.github.mayconr.juoserver.game.interaction.animation.AnimationHandler;
import com.github.mayconr.juoserver.game.interaction.speech.SpeechHandler;
import com.github.mayconr.juoserver.game.interaction.target.TargetHandler;
import com.github.mayconr.juoserver.game.interaction.target.TargetResult;
import com.github.mayconr.juoserver.game.model.AnimationOptions;
import com.github.mayconr.juoserver.game.model.CursorType;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.world.WorldModule;
import com.github.mayconr.juoserver.network.packet.ActionRequest;
import com.github.mayconr.juoserver.network.packet.Target;
import com.github.mayconr.juoserver.network.packet.UnicodeSpeachRequest;
import lombok.RequiredArgsConstructor;

import java.util.function.Consumer;

@RequiredArgsConstructor
public class InteractionModule implements WorldModule, InteractionCommands {

    private final TargetHandler targetHandler;
    private final ActionHandler actionHandler;
    private final AnimationHandler animationHandler;
    private final SpeechHandler speechHandler;

    @Override
    public void update(double delta) {

    }

    @Override
    public void sendTarget(UOPlayer player, CursorType type, Consumer<TargetResult> consumer) {
        targetHandler.sendTarget(player, type, consumer);
    }

    @Override
    public void resolveTarget(UOPlayer player, Target target) {
        targetHandler.resolveTarget(player, target);
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
