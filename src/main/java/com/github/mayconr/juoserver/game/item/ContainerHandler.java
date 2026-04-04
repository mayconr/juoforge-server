package com.github.mayconr.juoserver.game.item;

import com.github.mayconr.juoserver.game.model.Container;
import com.github.mayconr.juoserver.game.model.UOContainer;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.event.ItemDeleted;
import com.github.mayconr.juoserver.game.model.event.ItemUpdated;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

@RequiredArgsConstructor
public class ContainerHandler {

    private final EventBus eventBus;

    public List<UOItem> getItemsInContainer(Container container, Predicate<UOItem> predicate) {
        final List<UOItem> items = new ArrayList<>();
        for (UOItem item : container.getContainerItems()) {
            if (predicate.test(item)) {
                items.add(item);
            }
        }
        return items;
    }

    public int consumeItem(UOContainer container, String itemName, int amount, boolean searchNestedContainers) {
        if (amount <= 0) {
            return 0;
        }

        int available = countItems(container, itemName, searchNestedContainers);

        if (available < amount) {
            return -1;
        }

        return consumeInternal(container, itemName, amount, searchNestedContainers);
    }

    private int countItems(UOContainer container,
                           String itemName,
                           boolean searchNestedContainers) {
        int total = 0;
        for (UOItem item : container.getContainerItems()) {
            if (item.getName().equals(itemName)) {
                total += item.getAmount();
            }
            if (searchNestedContainers && item instanceof UOContainer nestedContainer) {
                total += countItems(nestedContainer, itemName, true);
            }
        }
        return total;
    }

    private int consumeInternal(UOContainer container,
                                String itemName,
                                int amount,
                                boolean searchNestedContainers) {

        Iterator<UOItem> iterator = container.getContainerItems().iterator();
        while (iterator.hasNext() && amount > 0) {
            UOItem item = iterator.next();
            if (item.getName().equals(itemName)) {
                int itemAmount = item.getAmount();
                if (itemAmount > amount) {
                    item.setAmount(itemAmount - amount);
                    eventBus.publish(new ItemUpdated(item, container));
                    return 0;
                } else {
                    amount -= itemAmount;
                    iterator.remove();
                    eventBus.publish(new ItemDeleted(item));
                    continue;
                }
            }
            if (searchNestedContainers && item instanceof UOContainer nestedContainer) {
                amount = consumeInternal(
                        nestedContainer,
                        itemName,
                        amount,
                        true
                );
                if (amount <= 0) {
                    return 0;
                }
            }
        }
        return 0;
    }
}
