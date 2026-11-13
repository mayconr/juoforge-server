package com.github.mayconr.juoserver.game.item.flow.drop.validation;

import com.github.mayconr.juoserver.game.item.flow.drop.DropItemContext;
import com.github.mayconr.juoserver.game.item.hold.HeldItems;
import com.github.mayconr.juoserver.game.model.ContainerLocation;
import com.github.mayconr.juoserver.game.model.GroundLocation;
import com.github.mayconr.juoserver.game.model.ItemFlag;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.storage.WorldStorage;
import java.util.HashSet;
import java.util.Objects;

/** Rejects recursive containment and incompatible stacks before either item is changed. */
public final class ValidateDropTargetStep extends AbstractFlowStep<DropItemContext> {
    private static final int MAX_STACK_AMOUNT = 60000;
    private final WorldStorage storage;
    private final HeldItems heldItems;

    public ValidateDropTargetStep(WorldStorage storage, HeldItems heldItems) {
        super("validate-drop-target");
        this.storage = storage;
        this.heldItems = heldItems;
    }

    @Override
    public StepResult execute(DropItemContext context) {
        if (context.isGroundDrop()) {
            return StepResult.success();
        }
        var target = context.isContainerDrop()
                ? context.getTargetContainer().container() : context.getTargetStack().itemStack();
        if (!hasSafeAncestry(context.getItem(), target)) {
            return StepResult.failure("DROP_INVALID_CONTAINMENT", "Destination ancestry is invalid or held by a player");
        }
        if (context.isStack() && !canStack(context.getItem(), target)) {
            return StepResult.failure("DROP_INVALID_STACK", "Destination cannot absorb this stack");
        }
        return StepResult.success();
    }

    private boolean hasSafeAncestry(UOItem dropped, UOItem target) {
        var visited = new HashSet<Integer>();
        var current = target;
        while (visited.add(current.getSerialId())) {
            if (current.getSerialId() == dropped.getSerialId() || heldItems.isHeld(current)) {
                return false;
            }
            if (!(current.getCurrentLocation() instanceof ContainerLocation inside)) {
                return true;
            }
            var parent = storage.items().getContainer(inside.containerSerialId());
            if (parent.isEmpty()) {
                return false;
            }
            current = parent.get();
        }
        return false;
    }

    private static boolean canStack(UOItem dropped, UOItem target) {
        return (target.getCurrentLocation() instanceof GroundLocation
                || target.getCurrentLocation() instanceof ContainerLocation)
                && dropped.hasFlag(ItemFlag.STACKABLE) && target.hasFlag(ItemFlag.STACKABLE)
                && dropped.getClass() == target.getClass()
                && Objects.equals(dropped.getTemplate(), target.getTemplate())
                && dropped.getModelId() == target.getModelId()
                && dropped.getHue() == target.getHue()
                && Objects.equals(dropped.getName(), target.getName())
                && dropped.getAmount() > 0 && target.getAmount() > 0
                && (long) dropped.getAmount() + target.getAmount() <= MAX_STACK_AMOUNT;
    }
}
