package com.github.mayconr.juoserver.game.item.flow.drop;

import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.UOContainer;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.infrastructure.flow.SyncFlowContext;
import com.github.mayconr.juoserver.network.packet.DropItem;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DropItemContext extends SyncFlowContext<Void> {
    private final UOPlayer player;

    private DropItem dropItem;
    private TargetContainer targetContainer;
    private TargetStack targetStack;
    private TargetGround targetGround;

    private UOItem item;

    private DropTarget target;

    private DropItemContext(UOPlayer player) {
        this.player = player;
    }

    // FACTORIES
    public static DropItemContext ofDropItem(UOPlayer player, DropItem dropItem) {
        var context = new DropItemContext(player);
        context.setDropItem(dropItem);
        return context;
    }

    public static DropItemContext ofStack(UOPlayer player, TargetStack targetStack) {
        var context = new DropItemContext(player);
        context.setTargetStack(targetStack);
        return context;
    }

    public static DropItemContext ofGround(UOPlayer player, UOItem item, TargetGround ground) {
        var context = new DropItemContext(player);
        context.setItem(item);
        context.setTargetGround(ground);
        return context;
    }

    public static DropItemContext ofContainer(UOPlayer player, TargetContainer targetContainer) {
        var context = new DropItemContext(player);
        context.setTargetContainer(targetContainer);
        return context;
    }

    public boolean isDroppedItem(DropTarget type) {
        return type.equals(this.target) && isDroppedItem();
    }

    public boolean isDroppedItem() {
        return dropItem != null;
    }

    public boolean isStack() {
        return targetStack != null;
    }

    public boolean isContainerDrop() {
        return targetContainer != null;
    }

    public boolean isGroundDrop() {
        return targetGround != null;
    }

    public record TargetContainer(UOContainer container, Location location) {
    }

    public record TargetStack(UOItem itemStack) {
    }

    public record TargetGround(Location location) {
    }

    public enum DropTarget {
        /**
         * Item dropped on the ground
         */
        GROUND,
        /**
         * Item dropped on another item. It can be a stack or container
         */
        ITEM,
        /**
         * Item dropped on a mobile
         */
        MOBILE;
    }
}
