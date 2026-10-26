package com.github.mayconr.juoserver.network.session.handler;

import com.github.mayconr.juoserver.network.session.SessionView;
import com.github.mayconr.juoserver.network.session.SessionPacketWriter;

import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.model.event.*;
import com.github.mayconr.juoserver.network.packet.*;
import com.github.mayconr.juoserver.game.world.WorldInternal;
import lombok.extern.slf4j.Slf4j;
import com.github.mayconr.juoserver.game.model.event.ItemStacked.StackDestination;
import java.util.concurrent.TimeUnit;

@Slf4j
public final class ItemEventHandler {
    private final SessionView session;
    private final SessionPacketWriter packetWriter;
    private final WorldInternal world;

    public ItemEventHandler(SessionView session, SessionPacketWriter packetWriter, WorldInternal world) {
        this.session = session;
        this.packetWriter = packetWriter;
        this.world = world;
    }

    public void onItemEquipped(ItemEquipped equipped) {
        final var item = equipped.item();
        if (session.shouldReceiveUpdate(equipped.mobile()) && item.getCurrentLocation() instanceof EquippedLocation) {
            packetWriter.schedule(packets -> {
                if (session.shouldReceiveUpdate(equipped.mobile())
                        && item.getCurrentLocation() instanceof EquippedLocation location
                        && location.ownerSerialId().equals(equipped.mobile().getSerialId())) {
                    packets.writeAndFlush(new EquipItem(equipped.mobile(), item.getLayer(), equipped.item()));
                }
            }, 20, TimeUnit.MILLISECONDS);
        }
    }

    public void onItemUnequipped(ItemUnequipped itemUnequipped) {
        if (session.shouldReceiveUpdate(itemUnequipped.mobile())) {
            packetWriter.writeAndFlush(new DrawMobile(itemUnequipped.mobile(), world.mobile().getEquippedItems(itemUnequipped.mobile())));
        }
    }

    public void onItemDroppedOnTheGround(ItemDroppedOnTheGround event) {
        if (session.shouldReceiveUpdate(event.item())) {
            packetWriter.writeAndFlush(new ObjectInfo(event.item()));
        }
    }

    public void onItemDroppedInContainer(ItemDroppedInContainer event) {
        final var container = event.container();

        // container has owner, must notify only this one
        if (container.getCurrentLocation() instanceof EquippedLocation(Integer ownerSerialId)) {
            if (session.player() != null && ownerSerialId == session.player().getSerialId()) {
                packetWriter.enqueue(packets -> packets.writeAndFlush(new AddItemToContainer(event.container(), event.item())));
            }
            return;
        }

        // Container in the ground
        if (canSeeContainer(event.container())) {
            packetWriter.enqueue(packets -> packets.writeAndFlush(new AddItemToContainer(event.container(), event.item())));
        }

    }

    public void onItemStacked(ItemStacked event) {
        // Removal must reach clients even if the dropped item has already changed location.
        packetWriter.writeAndFlush(new DeleteObject(event.dropped()));
        if (StackDestination.GROUND.equals(event.destination()) && session.shouldReceiveUpdate(event.target())) {
            packetWriter.writeAndFlush(new ObjectInfo(event.target()));
        }
        if (StackDestination.CONTAINER.equals(event.destination()) && canSeeContainer(event.container())) {
            packetWriter.writeAndFlush(new AddItemToContainer(event.container(), event.target()));
        }
    }

    public void onItemCreated(GroundedItemCreated event) {
        if (session.shouldReceiveUpdate(event.item())) {
            packetWriter.schedule(packets -> {
                if (event.item().getCurrentLocation() instanceof GroundLocation && session.shouldReceiveUpdate(event.item())) {
                    packets.writeAndFlush(new ObjectInfo(event.item()));
                }
            }, 20, TimeUnit.MILLISECONDS);
        }
    }

    public void onEquippedItemCreated(EquippedItemCreated event) {
        if (session.shouldReceiveUpdate(event.mobile())) {
            packetWriter.writeAndFlush(new DrawMobile(event.mobile(), world.mobile().getEquippedItems(event.mobile())));
        }
    }

    public void onItemCreatedInContainer(ItemCreatedInContainer event) {
        if (event.owner() != null) {
            if (session.isPlayer(event.owner())) {
                packetWriter.enqueue(packets -> packets.writeAndFlush(new AddItemToContainer(event.container(), event.item())));
            }
            return;
        }

        if (canSeeContainer(event.container())) {
            packetWriter.enqueue(packets -> packets.writeAndFlush(new AddItemToContainer(event.container(), event.item())));
        }
    }

    public void onItemDeleted(ItemDeleted event) {
        packetWriter.writeAndFlush(new DeleteObject(event.item()));
    }

    public void onItemUpdated(ItemUpdated event) {
        final var item = event.item();
        if (item.getCurrentLocation() instanceof ContainerLocation
                ? !canSeeContainer(event.container()) : !session.shouldReceiveUpdate(item)) {
            return;
        }
        packetWriter.write(new ObjectRevision(event.item()));
        if (item.getCurrentLocation() instanceof ContainerLocation) {
            packetWriter.writeAndFlush(new AddItemToContainer(event.container(), event.item()));
            return;
        }
        packetWriter.writeAndFlush(new ObjectInfo(event.item()));
    }

    private boolean canSeeContainer(UOContainer container) {
        UOItem current = container;
        var visited = new java.util.HashSet<Integer>();
        while (current != null && visited.add(current.getSerialId())) {
            if (current.getCurrentLocation() instanceof EquippedLocation location) {
                return session.player() != null && location.ownerSerialId().equals(session.player().getSerialId());
            }
            if (current.getCurrentLocation() instanceof GroundLocation) {
                return session.shouldReceiveUpdate(current);
            }
            if (!(current.getCurrentLocation() instanceof ContainerLocation location)) {
                return false;
            }
            current = world.item().getItemBySerialId(location.containerSerialId()).orElse(null);
        }
        return false;
    }
}
