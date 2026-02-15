package com.github.mayconr.juoserver.game.world.module.item;

import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.UOItem;

import java.util.Collection;

public interface ItemQueries {
    UOItem getItem(long itemId);

    Collection<UOItem> getItemsAt(Location location);
}
