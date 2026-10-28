package com.github.mayconr.juoserver.game.ai.registry;

import com.github.mayconr.juoserver.game.ai.definition.AIFlowContext;
import com.github.mayconr.juoserver.game.ai.definition.aggressive.AggressiveAIContext;
import com.github.mayconr.juoserver.game.ai.definition.animal.PassiveAnimalAIContext;
import com.github.mayconr.juoserver.game.ai.definition.vendor.VendorAIContext;
import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.game.world.World;

import java.util.Map;
import java.util.Set;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class AIContextRegistry {
    private final Map<String, AIContextFactory<?>> factories = new ConcurrentHashMap<>();

    public AIContextRegistry() {
        register("DIALOGUE_REACTIVE", (npc, world) -> new VendorAIContext(npc, world, npc.getStockType()));
        register("PASSIVE_ANIMAL", PassiveAnimalAIContext::new);
        register("AGGRESSIVE", AggressiveAIContext::new);
    }

    /** Registers a factory for a new AI type without replacing an existing registration. */
    public void register(String type, AIContextFactory<?> factory) {
        Objects.requireNonNull(type, "AI type is required");
        Objects.requireNonNull(factory, "AI context factory is required");
        if (type.isBlank()) throw new IllegalArgumentException("AI type must not be blank");
        if (factories.putIfAbsent(type, factory) != null) {
            throw new IllegalArgumentException("AI type already registered: " + type);
        }
    }

    /** Returns an immutable snapshot of registered AI types. */
    public Set<String> getRegisteredTypes() {
        return Set.copyOf(factories.keySet());
    }

    /** Creates a context, preserving the generic context fallback for unregistered types. */
    public AIFlowContext create(String type, UONpc npc, World world) {
        Objects.requireNonNull(type, "AI type is required");
        var factory = factories.get(type);
        return factory == null ? new AIFlowContext(npc, world)
                : Objects.requireNonNull(factory.create(npc, world), "AI context factory returned null for " + type);
    }
}
