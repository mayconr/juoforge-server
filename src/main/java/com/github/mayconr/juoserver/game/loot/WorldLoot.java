package com.github.mayconr.juoserver.game.loot;

import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOCorpse;
import com.github.mayconr.juoserver.game.item.ItemRequest;
import java.util.List;

public interface WorldLoot {
    /** Resolves and stores template-only drops once per corpse, without creating items. */
    List<ItemRequest> generate(LootRequest request);

    /** Creates pending drops on container opening; each plan is consumed at most once. */
    List<UOItem> materialize(UOCorpse corpse);
}
