package com.github.mayconr.juoserver.game.ai.session;

import com.github.mayconr.juoserver.game.ai.policy.AIActivationPolicy;
import com.github.mayconr.juoserver.game.ai.policy.AISpeechPolicy;

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
    private final AIActivationPolicy activationPolicy;
    private final AISpeechPolicy speechPolicy;

    @Override
    public void update(double delta) {
        if (!activationPolicy.isActive(context)) {
            context.discardSpeech(speech -> true);
            context.clearActions();
            return;
        }
        context.discardSpeech(speech -> !speechPolicy.accepts(context, speech));
        context.setDelta(delta);
        flows.execute(context);

        NpcAction action;
        while ((action = context.actions().poll()) != null) {
            dispatcher.accept(action);
        }
    }

    @Override
    public void onSpeech(MobileSpeech speech) {
        if (speechPolicy.accepts(context, speech) && activationPolicy.isActive(context)) {
            context.enqueueEvent(speech);
        }
    }
}
