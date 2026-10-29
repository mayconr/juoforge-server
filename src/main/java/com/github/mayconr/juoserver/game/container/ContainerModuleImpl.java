package com.github.mayconr.juoserver.game.container;

import com.github.mayconr.juoserver.game.container.flow.add.AddContainerItemContext;
import com.github.mayconr.juoserver.game.model.GroundLocation;
import com.github.mayconr.juoserver.game.model.EquippedLocation;
import com.github.mayconr.juoserver.game.model.ItemFlag;
import com.github.mayconr.juoserver.game.model.ItemLocation;

import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.ConsumeResult;
import com.github.mayconr.juoserver.game.item.ContainerHandler;
import java.util.List;
import java.util.function.Predicate;

import com.github.mayconr.juoserver.game.model.UOContainer;
import com.github.mayconr.juoserver.game.model.Container;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.item.flow.open.OpenContainerContext;
import com.github.mayconr.juoserver.game.world.context.ModuleContext;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import lombok.RequiredArgsConstructor;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@RequiredArgsConstructor
public class ContainerModuleImpl implements ContainerModule {
    private final RealmStorage storage;
    private final ContainerHandler containerHandler;

    @Override
    public List<UOItem> getItemsInContainer(
            Integer containerSerial, Predicate<UOItem> predicate) {
        return containerHandler.getItemsInContainer(containerSerial, predicate);
    }

    @Override
    public ConsumeResult consumeItem(
            Integer containerSerial, String name, int amount, boolean searchNestedContainers) {
        int remaining = containerHandler.consumeItem(containerSerial, name, amount, searchNestedContainers);
        return new ConsumeResult(remaining > -1, remaining);
    }

    @Override
    public java.util.Optional<UOContainer> getContainerBySerialId(int serial) {
        return storage.getContainer(serial);
    }

    @Override
    public UOItem addItem(UOContainer container, UOItem item, boolean tryStack, boolean notifyClient) {
        var context = new AddContainerItemContext(
                Objects.requireNonNull(container, "Container is required"),
                Objects.requireNonNull(item, "Item is required"), tryStack, notifyClient);
        var result = flows.execute(context);
        if (result.flowFailed()) {
            throw com.github.mayconr.juoserver.game.world.ModuleOperationException.fromFailure(
                    "addItem", result, "{item.creation_failed}");
        }
        return context.result();
    }

    private ModuleContext.FlowFacade flows;

    @Override
    public void initialize(ModuleContext context) {
        this.flows = context.flows();
    }

    @Override
    public void openContainer(UOPlayer player, Container container) {
        flows.execute(new OpenContainerContext(player, container));
    }


    @Override
    public long getWeight(UOContainer container) {
        Objects.requireNonNull(container, "Container is required");
        return Math.addExact((long) container.getUnitWeight() * container.getAmount(),
                getContentsWeight(container));
    }

    @Override
    public long getContentsWeight(UOContainer container) {
        return contentsWeight(Objects.requireNonNull(container, "Container is required"), new HashSet<>());
    }

    private long contentsWeight(UOContainer container, Set<Integer> visited) {
        if (!visited.add(container.getSerialId())) {
            throw new IllegalStateException("Repeated container or cycle at serial " + container.getSerialId());
        }
        long weight = 0;
        for (var serial : container.getContainerItems()) {
            var item = storage.getItem(serial).orElseThrow(() -> new IllegalStateException(
                    "Cannot calculate weight: item " + serial + " from container "
                            + container.getSerialId() + " is not loaded"));
            if (item instanceof UOContainer nested) {
                weight = Math.addExact(weight, contentsWeight(nested, visited));
            }
            weight = Math.addExact(weight, (long) item.getUnitWeight() * item.getAmount());
        }
        return weight;
    }
}
