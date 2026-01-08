package com.github.mayconr.juoserver.game.core.model;

import java.util.Collection;

public interface Container {

    void addItemToContainer(UOItem item);

    void removeItemFromContainer(UOItem item);

    Collection<UOItem> getItemsInContainer();

    int getSerialId();

    int getContainerGumpId();
}
