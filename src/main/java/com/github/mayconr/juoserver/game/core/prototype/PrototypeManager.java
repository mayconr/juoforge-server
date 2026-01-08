package com.github.mayconr.juoserver.game.core.prototype;

import java.util.Collection;
import java.util.Optional;

public interface PrototypeManager {
    Collection<ItemPrototype> getItems();

    Optional<ItemPrototype> getItemByName(String name);

    Optional<NpcPrototype> getNpcByName(String npcId);
}
