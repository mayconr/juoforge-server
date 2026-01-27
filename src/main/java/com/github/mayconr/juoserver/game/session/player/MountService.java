package com.github.mayconr.juoserver.game.session.player;

import com.github.mayconr.juoserver.game.model.Layer;
import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.session.SessionFanout;
import com.github.mayconr.juoserver.game.session.SessionOutbound;
import com.github.mayconr.juoserver.game.session.world.WorldSession;
import com.github.mayconr.juoserver.network.packet.DeleteObject;
import com.github.mayconr.juoserver.network.packet.DrawGamePlayer;
import com.github.mayconr.juoserver.network.packet.DrawMobile;
import com.github.mayconr.juoserver.network.packet.EquipItem;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class MountService {

    private final UOPlayer player;
    private final SessionOutbound outbound;
    private final SessionFanout fanout;
    private final WorldSession worldSession;

    public void handleMount(UONpc npc) {
        if (player.getEquippedItems().get(Layer.MOUNT) != null) {
            throw new IllegalStateException("Player " + player.getName() + " already mounted");
        }
        worldSession.createItemAtLocation(npc.getMountItemName(), player)
                .thenAccept(item->{
                    worldSession.deleteMobile(npc);
                    worldSession.deleteItem(item);

                    player.equipItem(Layer.MOUNT, item);
                    player.setLocation(npc);
                    player.setDirection(npc.getDirection());

                    fanout.write(new DeleteObject(npc));
                    fanout.writeAndFlush(new EquipItem(player, Layer.MOUNT, item)); // TODO filter by channels in range
                }).whenComplete(this::logging);


    }

    public void handleMount(String mount) {

    }

    public void handleUnmount() {
        final var mountItem = player.getEquippedItems().get(Layer.MOUNT);

        if (mountItem != null) {
            worldSession.createNpcAtLocation(mountItem.getMountNpc(), player)
                .thenAccept(npc -> {
                    npc.setDirection(player.getDirection());
                    player.unequipItem(mountItem);

                    worldSession.deleteItem(mountItem);

                    fanout.write(new DrawMobile(npc));
                    fanout.writeAndFlush(new DeleteObject(mountItem)); // TODO filter by channels in range
                }).whenComplete(this::logging);

        }
    }

    private <T> void logging(T value, Throwable throwable) {
        if (throwable != null) {
            log.error("Error to handle mount", throwable);
        }
    }
}
