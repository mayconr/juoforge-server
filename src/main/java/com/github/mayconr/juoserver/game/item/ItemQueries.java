package com.github.mayconr.juoserver.game.item;

import com.github.mayconr.juoserver.game.model.Container;
import com.github.mayconr.juoserver.game.model.UOItem;

import java.util.List;
import java.util.function.Predicate;

public interface ItemQueries {

    List<UOItem> getItemsInContainer(Container container, Predicate<UOItem> predicate);
}
