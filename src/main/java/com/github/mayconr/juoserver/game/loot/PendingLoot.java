package com.github.mayconr.juoserver.game.loot;

import com.github.mayconr.juoserver.infrastructure.serialization.DataType;
import com.github.mayconr.juoserver.infrastructure.serialization.DataMapper;
import com.github.mayconr.juoserver.game.item.ItemRequest;
import com.github.mayconr.juoserver.game.model.UOCorpse;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Template snapshots stored as plain data in the corpse's persistent attributes. */
public final class PendingLoot {
    public static final String TEMPLATES = "loot.pendingTemplates";
    private static final DataType<List<Map<String, Object>>> STORED_PLAN = new DataType<>() { };
    private static final DataType<List<ItemRequest>> ITEM_REQUESTS = new DataType<>() { };
    private final DataMapper dataMapper;

    public PendingLoot(DataMapper dataMapper) {
        this.dataMapper = Objects.requireNonNull(dataMapper, "DataMapper is required");
    }

    public void store(UOCorpse corpse, List<ItemRequest> plan) {
        corpse.persistentAttributes().set(TEMPLATES, dataMapper.convert(plan, STORED_PLAN));
    }

    public List<ItemRequest> read(UOCorpse corpse) {
        var stored = corpse.persistentAttributes().get(TEMPLATES);
        if (stored == null) return List.of();
        // Attribute loading yields nested maps, not ItemRequest/ItemTemplate instances.
        return List.copyOf(dataMapper.convert(stored, ITEM_REQUESTS));
    }

    public void clear(UOCorpse corpse) {
        corpse.persistentAttributes().remove(TEMPLATES);
    }
}
