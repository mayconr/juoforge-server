package com.github.mayconr.juoserver.game.world.module.iteraction;

import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.world.WorldModule;
import com.github.mayconr.juoserver.game.world.module.iteraction.action.ActionHandler;
import com.github.mayconr.juoserver.game.world.module.iteraction.animation.AnimationHandler;
import com.github.mayconr.juoserver.game.world.module.iteraction.movement.MovementHandler;
import com.github.mayconr.juoserver.game.world.module.iteraction.speech.SpeechHandler;
import com.github.mayconr.juoserver.game.world.module.iteraction.target.TargetHandler;
import com.github.mayconr.juoserver.game.world.module.iteraction.target.TargetResult;
import com.github.mayconr.juoserver.network.packet.ActionRequest;
import com.github.mayconr.juoserver.network.packet.MoveRequest;
import com.github.mayconr.juoserver.network.packet.Target;
import com.github.mayconr.juoserver.network.packet.UnicodeSpeachRequest;
import lombok.RequiredArgsConstructor;

import java.util.function.Consumer;

@RequiredArgsConstructor
public class InteractionModule implements WorldModule, InteractionCommands {

    private final MovementHandler movementHandler;
    private final SpeechHandler speechHandler;
    private final TargetHandler targetHandler;
    private final ActionHandler actionHandler;
    private final AnimationHandler animationHandler;

    @Override
    public void update(long tick, double delta) {

    }

    @Override
    public void move(UOPlayer player, MoveRequest request) {
        movementHandler.move(player, request);
    }

    @Override
    public void move(UOPlayer player, Location location) {
        movementHandler.move(player, location);
    }

    @Override
    public void speech(UOPlayer player, UnicodeSpeachRequest request) {
        speechHandler.speech(player, request);
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
}
