package com.github.mayconr.juoserver.game.world;

import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.network.packet.ActionRequest;
import com.github.mayconr.juoserver.network.packet.Target;
import com.github.mayconr.juoserver.network.packet.UnicodeSpeachRequest;

/** Client interaction events handled by the engine. */
public interface InteractionInternal {
    void resolveTarget(UOPlayer player, Target target);

    void handleAction(UOPlayer player, ActionRequest request);

    void speech(UOPlayer player, UnicodeSpeachRequest request);
}
