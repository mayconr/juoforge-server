package com.github.mayconr.juoserver.game.ai;

import com.github.mayconr.juoserver.game.ai.definition.AIFlowContext;
import com.github.mayconr.juoserver.game.model.event.MobileSpeech;

public interface AISession<T extends AIFlowContext> {

    void update(double delta);

    void onSpeech(MobileSpeech speech);
}
