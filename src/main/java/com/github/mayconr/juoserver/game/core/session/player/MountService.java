package com.github.mayconr.juoserver.game.core.session.player;

import com.github.mayconr.juoserver.game.core.model.Layer;
import com.github.mayconr.juoserver.game.core.model.UONpc;
import com.github.mayconr.juoserver.game.core.model.UOPlayer;
import com.github.mayconr.juoserver.game.packet.DeleteObject;
import com.github.mayconr.juoserver.game.packet.DrawGamePlayer;
import com.github.mayconr.juoserver.game.packet.DrawMobile;
import com.github.mayconr.juoserver.game.packet.EquipItem;
import com.github.mayconr.juoserver.game.storage.WorldService;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.group.ChannelGroup;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MountService {

    private final UOPlayer player;
    private final ChannelHandlerContext ctx;
    private final ChannelGroup channelGroup;
    private final WorldService worldService;

    public void handleMount(UONpc npc) {
        handleMount(npc.getMount());

        worldService.deleteMobile(npc);
        player.setLocation(npc);
        player.setDirection(npc.getDirection());

        channelGroup.write(new DeleteObject(npc));
        ctx.writeAndFlush(new DrawGamePlayer(player));
    }

    public void handleMount(String mount) {
        if (player.getEquippedItems().get(Layer.MOUNT) != null) {
            throw new IllegalStateException("Player " + player.getName() + " already mounted");
        }
        final var item = worldService.createItem(mount, player);
        player.equipItem(Layer.MOUNT, item);
        channelGroup.writeAndFlush(
                new EquipItem(player, Layer.MOUNT, item)); // TODO filter by channels in range
    }

    public void handleUnmount() {
        final var mount = player.getEquippedItems().get(Layer.MOUNT);

        if (mount != null) {
            worldService.deleteItem(mount);
            final var npc = worldService.createNpcAtLocation(mount.getMountNpc(), player);
            npc.setDirection(player.getDirection());
            player.unequipItem(mount);

            channelGroup.write(new DrawMobile(npc));
            channelGroup.writeAndFlush(new DeleteObject(mount)); // TODO filter by channels in range
        }
    }
}
