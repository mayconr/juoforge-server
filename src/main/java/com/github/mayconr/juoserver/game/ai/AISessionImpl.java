package com.github.mayconr.juoserver.game.ai;

import com.github.mayconr.juoserver.game.ai.actions.NpcAction;
import com.github.mayconr.juoserver.game.ai.definition.AIFlowContext;
import com.github.mayconr.juoserver.game.model.event.MobileSpeech;
import com.github.mayconr.juoserver.game.world.context.ModuleContext;
import lombok.RequiredArgsConstructor;

import java.util.function.Consumer;

@RequiredArgsConstructor
public class AISessionImpl<T extends AIFlowContext> implements AISession<T> {
    private final ModuleContext.FlowFacade flows;
    private final T context;
    private final Consumer<NpcAction> dispatcher;

    @Override
    public void update(double delta) {
        context.setDelta(delta);
        flows.execute(context);

        NpcAction action;
        while ((action = context.actions().poll()) != null) {
            dispatcher.accept(action);
        }
    }

    @Override
    public void onSpeech(MobileSpeech speech) {
        context.enqueueEvent(speech);
    }
}
