package com.github.mayconr.juoserver.game.ai.session;

import com.github.mayconr.juoserver.game.ai.definition.AIFlowContext;
import com.github.mayconr.juoserver.game.model.event.MobileSpeech;

public interface AISession<T extends AIFlowContext> {

    /** Enables execution and speech handling without resetting the session. */
    void activate();

    /** Pauses execution and speech handling while preserving the session. */
    void deactivate();

    void update(double delta);

    void onSpeech(MobileSpeech speech);
}
