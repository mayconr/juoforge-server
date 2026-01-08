package com.github.mayconr.juoserver.game.core.prototype;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class PrototypeManagerImpl implements PrototypeManager {

    private final Map<String, ItemPrototype> nameItemPrototypeMap = new HashMap<>();
    private final Map<String, NpcPrototype> npcPrototypeMap = new HashMap<>();

    public PrototypeManagerImpl() {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try (InputStream in = new FileInputStream("prototype/items.yaml")) {
            final List<ItemPrototype> items =
                    mapper.readValue(
                            in,
                            mapper.getTypeFactory()
                                    .constructCollectionType(List.class, ItemPrototype.class));
            for (ItemPrototype item : items) {
                nameItemPrototypeMap.put(item.getName(), item);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (InputStream in = new FileInputStream("prototype/npcs.yaml")) {
            final List<NpcPrototype> npcs =
                    mapper.readValue(
                            in,
                            mapper.getTypeFactory()
                                    .constructCollectionType(List.class, NpcPrototype.class));
            for (NpcPrototype npc : npcs) {
                npcPrototypeMap.put(npc.getName(), npc);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Collection<ItemPrototype> getItems() {
        return nameItemPrototypeMap.values();
    }

    @Override
    public Optional<ItemPrototype> getItemByName(String name) {
        return Optional.ofNullable(nameItemPrototypeMap.get(name));
    }

    @Override
    public Optional<NpcPrototype> getNpcByName(String npcId) {
        return Optional.ofNullable(npcPrototypeMap.get(npcId));
    }
}
