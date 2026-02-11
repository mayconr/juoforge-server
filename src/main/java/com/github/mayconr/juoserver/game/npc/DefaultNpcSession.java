package com.github.mayconr.juoserver.game.npc;

import com.github.mayconr.juoserver.game.event.EventBus;
import com.github.mayconr.juoserver.game.event.EventHandler;
import com.github.mayconr.juoserver.game.event.GameEvent;
import com.github.mayconr.juoserver.game.model.TextType;
import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.event.MobileSpeech;
import com.github.mayconr.juoserver.game.model.event.SpeechContext;
import com.github.mayconr.juoserver.game.model.event.SpeechRange;
import com.github.mayconr.juoserver.game.npc.action.BuyListAction;
import com.github.mayconr.juoserver.game.npc.action.NpcAction;
import com.github.mayconr.juoserver.game.npc.action.SayAction;
import com.github.mayconr.juoserver.game.npc.ai.NpcAI;
import com.github.mayconr.juoserver.game.npc.behavior.NpcBehavior;
import com.github.mayconr.juoserver.game.npc.profile.BehaviorProfile;
import com.github.mayconr.juoserver.game.player.SessionFanout;
import com.github.mayconr.juoserver.game.world.WorldInternal;
import lombok.RequiredArgsConstructor;

import java.util.*;

@RequiredArgsConstructor
public class DefaultNpcSession implements NpcSession, NpcContext {

    private final Queue<NpcAction> actionQueue = new ArrayDeque<>();
    private final Map<String, Object> memory = new HashMap<>();

    private final UONpc npc;
    private final SessionFanout fanout;
    private final EventBus eventBus;
    private final BehaviorProfile profile;
    private final NpcAI ai;

    private WorldInternal world;
    private NpcBehavior current;

    public void initialize(WorldInternal world) {
        this.world = world;
        // Initialize behavior
        current = ai.decide(this, profile);
        current.initialize(this);

        // Register speech listener for players
        eventBus.register(MobileSpeech.class, new SpeechListener(world), speech -> speech.mobile() instanceof UOPlayer);
    }

    @Override
    public WorldInternal world() {
        return world;
    }

    @Override
    public UONpc npc() {
        return npc;
    }

    @Override
    public void think(double delta) {
        // TODO check is session is alive
        current.onThink(delta);
        while (!actionQueue.isEmpty()) {
            switch (actionQueue.poll()) {
                case SayAction say -> {
                    eventBus.publish(new MobileSpeech(npc, say.text(), new SpeechContext(TextType.NORMAL, SpeechRange.NORMAL, 0,0, System.currentTimeMillis(), npc)));
                    //fanout.writeAndFlush(new SendSpeech(TextType.NORMAL, 2046, npc.getSerialId(), npc.getModelId(), 1, npc.getDisplayName(), say.text()));
                }
                case BuyListAction buyList -> {
                    world.sendBuyGump(buyList.buyer(), npc, Collections.emptyList());
                }
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

        private final WorldInternal world;

        @Override
        public void handle(MobileSpeech event) {
            final var player = (UOPlayer) event.mobile();
            final var radius = (int) npc.getAttrMap().getOrDefault("behavior.radius", 1);

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
        var ctx = DefaultNpcSession.this;

        ai.onEvent(ctx, event);
        var next = ai.decide(ctx, profile);
        if (next != current) {
            current.onExit(ctx);
            current = next;
            current.initialize(ctx);
        }
    }
}
