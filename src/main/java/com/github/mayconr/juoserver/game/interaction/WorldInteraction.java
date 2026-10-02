package com.github.mayconr.juoserver.game.interaction;

import com.github.mayconr.juoserver.game.model.AnimationOptions;
import com.github.mayconr.juoserver.game.model.CursorType;
import com.github.mayconr.juoserver.game.model.TargetResult;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UOPlayer;

import java.util.function.Consumer;

/** Interaction operations available to shard code, without client packet handlers. */
public interface WorldInteraction {
    void sendTarget(UOPlayer player, CursorType type, Consumer<TargetResult> consumer);

    void sendAnimation(UOMobile mobile, AnimationOptions options);
}
