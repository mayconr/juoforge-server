package com.github.mayconr.juoserver.game.loot;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mayconr.juoserver.game.item.ItemRequest;
import com.github.mayconr.juoserver.game.model.UOCorpse;
import java.util.List;
import java.util.Map;

/** JSON-compatible template snapshots stored with the corpse's persistent attributes. */
public final class PendingLoot {
    public static final String TEMPLATES = "loot.pendingTemplates";
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final TypeReference<List<Map<String, Object>>> STORED_PLAN = new TypeReference<>() { };
    private static final TypeReference<List<ItemRequest>> ITEM_REQUESTS = new TypeReference<>() { };

    private PendingLoot() { }

    public static void store(UOCorpse corpse, List<ItemRequest> plan) {
        corpse.persistentAttributes().set(TEMPLATES, MAPPER.convertValue(plan, STORED_PLAN));
    }

    public static List<ItemRequest> read(UOCorpse corpse) {
        var stored = corpse.persistentAttributes().get(TEMPLATES);
        if (stored == null) return List.of();
        // Attribute loading yields nested maps, not ItemRequest/ItemTemplate instances.
        return List.copyOf(MAPPER.convertValue(stored, ITEM_REQUESTS));
    }

    public static void clear(UOCorpse corpse) {
        corpse.persistentAttributes().remove(TEMPLATES);
    }
}
