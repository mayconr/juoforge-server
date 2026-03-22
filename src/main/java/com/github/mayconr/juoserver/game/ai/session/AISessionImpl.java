package com.github.mayconr.juoserver.game.ai.session;

import com.github.mayconr.juoserver.game.ai.AI;
import com.github.mayconr.juoserver.game.ai.AIContext;
import com.github.mayconr.juoserver.game.ai.Behavior;
import com.github.mayconr.juoserver.game.ai.BehaviorProfile;
import com.github.mayconr.juoserver.game.ai.actions.NpcAction;
import com.github.mayconr.juoserver.game.ai.actions.SellListAction;
import com.github.mayconr.juoserver.game.ai.actions.SpeechAction;
import com.github.mayconr.juoserver.game.ai.actions.WalkAction;
import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.event.MobileSpeech;
import com.github.mayconr.juoserver.game.world.World;
import com.github.mayconr.juoserver.game.world.WorldView;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventHandler;
import com.github.mayconr.juoserver.infrastructure.eventbus.GameEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

@Slf4j
@RequiredArgsConstructor
public class AISessionImpl implements AISession, AIContext {

    private final Queue<NpcAction> actionQueue = new ArrayDeque<>();
    private final Map<String, Object> memory = new HashMap<>();

    private final UONpc npc;
    private final EventBus eventBus;
    private final BehaviorProfile profile;
    private final AI ai;
    private Behavior current;
    private World world;
    private SpeechListener speechListener;
    private boolean awake;

    @Override
    public void wakeup(World world) {
        this.world = world;
        this.speechListener = new SpeechListener(world);

        current = ai.decide(this, profile);
        if (current == null) {
            log.warn("AI session not awake, behavior not found");
            return;
        }
        current.initialize(this);

        // Register speech listener for players
        eventBus.register(MobileSpeech.class, speechListener, speech -> speech.mobile() instanceof UOPlayer);

        // AI will start to tick
        awake = true;
        log.info("NPC [{}] ai wakeup", npc.getName());
    }

    @Override
    public void kill() {
        awake = false;
        eventBus.unregister(MobileSpeech.class, speechListener);
    }

    @Override
    public World world() {
        return world;
    }

    @Override
    public UONpc npc() {
        return npc;
    }

    @Override
    public void think(double delta) {
        if (!awake) {
            return;
        }

        current.onThink(delta);
        while (!actionQueue.isEmpty()) {
            switch (actionQueue.poll()) {
                case SpeechAction say -> world.printTextAbove(npc, say.content(), say.speechTo());
                case SellListAction buyList -> world.beginVendorPurchase(buyList.buyer(), npc, buyList.itemsToSell());
                case WalkAction walkAction -> world.move(walkAction.npc(), walkAction.direction());
            }
        }
    }

    @Override
    public <T> void set(String key, T value) {
        memory.put(key, value);
    }

    @Override
    public <T> T get(String key, Class<T> type) {
        return (T) memory.get(key);
    }

    @Override
    public boolean has(String key) {
        return memory.containsValue(key);
    }

    @Override
    public void remove(String key) {
        memory.remove(key);
    }

    @Override
    public void enqueue(NpcAction action) {
        actionQueue.add(action);
    }

    @RequiredArgsConstructor
    private class SpeechListener implements EventHandler<MobileSpeech> {

        private final WorldView world;

        @Override
        public void handle(MobileSpeech event) {
            final var player = (UOPlayer) event.mobile();
            final var radius = (int) npc.persistentAttributes().getOrDefault("behavior.radius", 1);

            if (!world.isInRange(npc, player, radius)) {
                return;
            }

            switchBehaviorWhenDecided(event);

            // behavior react
            current.onSpeech(player, event.message());
        }
    }

    /**
     * Processes a game event and updates the NPC behavior if a transition is required.
     *
     * <p>This method forwards the received {@link GameEvent} to the AI system,
     * allowing it to update its internal state. After that, the AI evaluates
     * the current context and profile to decide which behavior should be active.</p>
     *
     * <p>If the decided behavior differs from the current one, the method performs
     * a behavior transition by calling {@code onExit} on the current behavior,
     * updating the active behavior reference, and initializing the new behavior
     * with the current NPC context.</p>
     *
     * <p>This method is responsible only for behavior switching logic and does not
     * execute behavior actions directly.</p>
     *
     * @param event the game event that may influence the NPC behavior decision
     */
    private void switchBehaviorWhenDecided(GameEvent event) {
        var ctx = AISessionImpl.this;

        ai.onEvent(ctx, event);
        var next = ai.decide(ctx, profile);
        if (next != current) {
            current.onExit(ctx);
            current = next;
            current.initialize(ctx);
        }
    }
}
