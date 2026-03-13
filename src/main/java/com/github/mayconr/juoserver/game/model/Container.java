package com.github.mayconr.juoserver.game.model;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface Container {

    UUID getId();

    void addItemsToContainer(List<UOItem> items);

    void addItemToContainer(UOItem item);

    void removeItemFromContainer(UOItem item);

    Collection<UOItem> getItemsInContainer();

    int getSerialId();

    int getContainerGumpId();

}
