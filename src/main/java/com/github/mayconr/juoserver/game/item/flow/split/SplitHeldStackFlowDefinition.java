package com.github.mayconr.juoserver.game.item.flow.split;

import com.github.mayconr.juoserver.game.container.WorldContainer;
import com.github.mayconr.juoserver.game.model.ContainerLocation;
import com.github.mayconr.juoserver.game.model.DefaultAttributeMap;
import com.github.mayconr.juoserver.game.model.EquippedLocation;
import com.github.mayconr.juoserver.game.model.GroundLocation;
import com.github.mayconr.juoserver.game.model.ItemFlag;
import com.github.mayconr.juoserver.game.model.UOContainer;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.event.GroundedItemCreated;
import com.github.mayconr.juoserver.game.model.event.ItemCreatedInContainer;
import com.github.mayconr.juoserver.game.model.event.ItemUpdated;
import com.github.mayconr.juoserver.infrastructure.eventbus.GameEvent;
import com.github.mayconr.juoserver.game.world.context.FlowRegistryFactory.GameInfra;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.FlowFactory;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import java.util.List;
import java.util.UUID;

public final class SplitHeldStackFlowDefinition {
    private SplitHeldStackFlowDefinition() {
    }

    public static Flow<SplitHeldStackContext> build(GameInfra infra, WorldContainer containers) {
        return FlowFactory.<SplitHeldStackContext>builder()
                .step(new SplitStackStep(infra, containers))
                .build();
    }

    private static final class SplitStackStep extends AbstractFlowStep<SplitHeldStackContext> {
        private final GameInfra infra;
        private final WorldContainer containers;

        private SplitStackStep(GameInfra infra, WorldContainer containers) {
            super("split-held-stack");
            this.infra = infra;
            this.containers = containers;
        }

        @Override
        public StepResult execute(SplitHeldStackContext context) {
            var item = context.getItem();
            int requested = context.getAmount();
            if (requested <= 0 || requested >= item.getAmount() || item instanceof UOContainer
                    || item.getFlags() == null || !item.hasFlag(ItemFlag.STACKABLE)) {
                return StepResult.failure("INVALID_STACK_SPLIT", "Invalid item or quantity for a stack split");
            }

            UOContainer parent = null;
            if (item.getCurrentLocation() instanceof ContainerLocation location) {
                parent = infra.storage().items().getContainer(location.containerSerialId()).orElse(null);
                if (parent == null || !parent.getContainerItems().contains(item.getSerialId())) {
                    return StepResult.failure("STACK_PARENT_MISSING", "Stack is missing from its origin container");
                }
            } else if (!(item.getCurrentLocation() instanceof GroundLocation)) {
                return StepResult.failure("INVALID_STACK_LOCATION", "Stack must be on the ground or in a container");
            }

            int originalAmount = item.getAmount();
            var remainder = createRemainder(item, originalAmount - requested);
            var remainderCreated = remainderCreatedEvent(remainder, parent);
            context.getUpdateHeldAmount().accept(requested);
            try {
                placeRemainder(remainder, parent);
            } catch (RuntimeException failure) {
                context.getUpdateHeldAmount().accept(originalAmount);
                throw failure;
            }
            infra.eventBus().publish(new ItemUpdated(item, parent, null, context.getPlayer()));
            infra.eventBus().publish(remainderCreated);
            return StepResult.success();
        }

        private UOItem createRemainder(UOItem original, int amount) {
            var data = original.toData();
            data.setSerialId(infra.serialGenerator().getNextItem());
            data.setId(UUID.randomUUID());
            data.setAmount(amount);
            data.setFlags(List.copyOf(data.getFlags()));
            data.setPersistentAttrMap(new DefaultAttributeMap(data.getPersistentAttrMap().toMap()));
            return new UOItem(data, original.getTemplate());
        }

        private void placeRemainder(UOItem remainder, UOContainer parent) {
            var items = infra.storage().items();
            try {
                items.cache(remainder);
                if (parent != null) {
                    containers.addItem(parent, remainder, false, false);
                }
            } catch (RuntimeException failure) {
                try {
                    items.delete(remainder);
                } catch (RuntimeException cleanupFailure) {
                    failure.addSuppressed(cleanupFailure);
                }
                throw failure;
            }
        }

        private GameEvent remainderCreatedEvent(UOItem remainder, UOContainer parent) {
            if (parent == null) {
                return new GroundedItemCreated(remainder);
            }
            var owner = parent.getCurrentLocation() instanceof EquippedLocation location
                    ? infra.storage().mobiles().get(location.ownerSerialId()).orElse(null) : null;
            return new ItemCreatedInContainer(parent, remainder, owner);
        }
    }
}
