package com.github.mayconr.juoserver.game.container;

import com.github.mayconr.juoserver.game.container.flow.add.AddContainerItemContext;
import com.github.mayconr.juoserver.game.container.flow.count.CountContainerItemsContext;
import com.github.mayconr.juoserver.game.container.flow.count.ValidateCountItemsRequestStep;
import com.github.mayconr.juoserver.game.container.flow.weight.ContainerWeightContext;
import com.github.mayconr.juoserver.game.container.flow.weight.ValidateContainerWeightRequestStep;
import com.github.mayconr.juoserver.game.container.flow.query.GetContainerItemsContext;
import com.github.mayconr.juoserver.game.model.GroundLocation;
import com.github.mayconr.juoserver.game.model.EquippedLocation;
import com.github.mayconr.juoserver.game.model.ItemFlag;
import com.github.mayconr.juoserver.game.model.ItemLocation;

import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.ConsumeResult;
import com.github.mayconr.juoserver.game.item.ContainerHandler;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import com.github.mayconr.juoserver.game.model.UOContainer;
import com.github.mayconr.juoserver.game.model.Container;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.container.flow.open.OpenContainerContext;
import com.github.mayconr.juoserver.game.world.ModuleOperationException;
import com.github.mayconr.juoserver.game.world.context.ModuleContext;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.storage.WorldStorage;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

@RequiredArgsConstructor
public class ContainerModuleImpl implements ContainerModule {
    private final WorldStorage storage;
    private final ContainerHandler containerHandler;

    @Override
    public List<UOItem> getItemsInContainer(
            Integer containerSerial, Predicate<UOItem> predicate, boolean searchNestedContainers) {
        var context = new GetContainerItemsContext(containerSerial, predicate, searchNestedContainers);
        var result = flows.execute(context);
        if (result.flowFailed()) {
            throw new IllegalArgumentException(result.reason(), result.cause());
        }
        return context.result();
    }

    @Override
    public ConsumeResult consumeItem(
            Integer containerSerial, String name, int amount, boolean searchNestedContainers) {
        int remaining = containerHandler.consumeItem(containerSerial, name, amount, searchNestedContainers);
        return new ConsumeResult(remaining > -1, remaining);
    }

    @Override
    public Optional<UOContainer> getContainerBySerialId(int serial) {
        return storage.getContainer(serial);
    }

    @Override
    public UOItem addItem(UOContainer container, UOItem item, boolean tryStack, boolean notifyClient) {
        var context = new AddContainerItemContext(
                Objects.requireNonNull(container, "Container is required"),
                Objects.requireNonNull(item, "Item is required"), tryStack, notifyClient);
        var result = flows.execute(context);
        if (result.flowFailed()) {
            throw ModuleOperationException.fromFailure(
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
        openContainer(player, container, false);
    }

    @Override
    public void openContainer(UOPlayer player, Container container, boolean equipBeforeOpening) {
        flows.execute(new OpenContainerContext(player, container, equipBeforeOpening));
    }


    @Override
    public int countItems(UOContainer container, String name, CountItemsOptions options) {
        var context = new CountContainerItemsContext(container, name, options);
        var result = flows.execute(context);
        if (result.flowFailed()) {
            throw countItemsFailure(result);
        }
        return context.result();
    }

    private RuntimeException countItemsFailure(StepResult result) {
        if (ValidateCountItemsRequestStep.INVALID_REQUEST.equals(result.code())) {
            return new IllegalArgumentException(result.reason());
        }
        return new IllegalStateException(result.reason(), result.cause());
    }

    @Override
    public long getContentsWeight(UOContainer container) {
        var context = new ContainerWeightContext(container);
        var result = flows.execute(context);
        if (result.flowFailed()) {
            throw contentsWeightFailure(result);
        }
        return context.result();
    }

    private RuntimeException contentsWeightFailure(StepResult result) {
        if (ValidateContainerWeightRequestStep.INVALID_REQUEST.equals(result.code())) {
            return new IllegalArgumentException(result.reason());
        }
        return new IllegalStateException(result.reason(), result.cause());
    }
}
