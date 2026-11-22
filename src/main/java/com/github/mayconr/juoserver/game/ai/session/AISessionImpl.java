package com.github.mayconr.juoserver.game.ai.session;

import com.github.mayconr.juoserver.game.ai.policy.AIExecutionPolicy;
import com.github.mayconr.juoserver.game.ai.policy.AISpeechPolicy;

import com.github.mayconr.juoserver.game.ai.actions.NpcAction;
import com.github.mayconr.juoserver.game.ai.definition.AIFlowContext;
import com.github.mayconr.juoserver.game.model.event.MobileSpeech;
import com.github.mayconr.juoserver.game.world.context.ModuleContext;
import lombok.RequiredArgsConstructor;

import java.util.function.Consumer;

/**
 * Runs the AI flow for one NPC. On each update, an active session checks the execution policy
 * before running the flow and dispatching the actions it produces.
 * Incoming speech is queued only while the session is active and both policies allow it.
 * The speech policy is checked again before the flow runs.
 * Deactivation or a blocked update clears pending actions and speech.
 */
@RequiredArgsConstructor
public class AISessionImpl<T extends AIFlowContext> implements AISession<T> {
    private final ModuleContext.FlowFacade flows;
    private final T context;
    private final Consumer<NpcAction> dispatcher;
    private final AIExecutionPolicy executionPolicy;
    private final AISpeechPolicy speechPolicy;
    private volatile boolean activated;

    @Override
    public void activate() {
        activated = true;
    }

    @Override
    public void deactivate() {
        activated = false;
        context.clearActions();
    }

    @Override
    public void update(double delta) {
        if (!activated) return;
        if (!executionPolicy.canRun(context)) {
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
        if (activated && speechPolicy.accepts(context, speech) && executionPolicy.canRun(context)) {
            context.enqueueEvent(speech);
        }
    }
}
