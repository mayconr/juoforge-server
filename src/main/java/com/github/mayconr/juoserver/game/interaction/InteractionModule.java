package com.github.mayconr.juoserver.game.interaction;

import com.github.mayconr.juoserver.game.model.TargetResult;
import com.github.mayconr.juoserver.game.model.AnimationOptions;
import com.github.mayconr.juoserver.game.model.CursorType;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.world.WorldModule;
import com.github.mayconr.juoserver.network.packet.ActionRequest;
import com.github.mayconr.juoserver.network.packet.Target;
import com.github.mayconr.juoserver.network.packet.UnicodeSpeachRequest;

import java.util.function.Consumer;

public interface InteractionModule extends WorldModule {

    void sendTarget(UOPlayer player, CursorType type, Consumer<TargetResult> consumer);

    void resolveTarget(UOPlayer player, Target target);

    void handleAction(UOPlayer player, ActionRequest request);

    void sendAnimation(UOMobile mobile, AnimationOptions options);

    void speech(UOPlayer player, UnicodeSpeachRequest request);
}
