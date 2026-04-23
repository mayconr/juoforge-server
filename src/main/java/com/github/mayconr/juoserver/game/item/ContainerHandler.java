package com.github.mayconr.juoserver.game.item;

import com.github.mayconr.juoserver.game.model.Container;
import com.github.mayconr.juoserver.game.model.UOContainer;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.event.ItemDeleted;
import com.github.mayconr.juoserver.game.model.event.ItemUpdated;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

@RequiredArgsConstructor
public class ContainerHandler {

    private final EventBus eventBus;
    private final RealmStorage storage;

    public List<UOItem> getItemsInContainer(Integer containerSerial, Predicate<UOItem> predicate) {
        var container = storage.getContainer(containerSerial).orElseThrow(()->new IllegalArgumentException("Container not found"));
        final List<UOItem> items = new ArrayList<>();
        for (Integer serial : container.getContainerItems()) {
            var item = storage.getItem(serial).orElseThrow(()->new IllegalArgumentException("Item not found"));
            if (predicate.test(item)) {
                items.add(item);
            }
        }
        return items;
    }

    public int consumeItem(Integer containerSerial, String itemName, int amount, boolean searchNestedContainers) {
        var container = storage.getContainer(containerSerial).orElseThrow(()->new IllegalArgumentException("Container not found"));

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
        for (Integer serial : container.getContainerItems()) {
            var item = storage.getItem(serial).orElseThrow(()->new IllegalArgumentException("Item not found"));
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

        Iterator<Integer> iterator = container.getContainerItems().iterator();
        while (iterator.hasNext() && amount > 0) {
            Integer serial = iterator.next();
            var item =  storage.getItem(serial).orElseThrow(()->new IllegalArgumentException("Item not found"));
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
