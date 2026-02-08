package com.github.mayconr.juoserver.game.session.player.click;

import com.github.mayconr.juoserver.game.session.player.item.PlayerItemService;
import com.github.mayconr.juoserver.game.trigger.item.ItemUseContext;
import com.github.mayconr.juoserver.game.trigger.item.ItemUseService;
import com.github.mayconr.juoserver.game.trigger.Trigger;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.session.SessionOutbound;
import com.github.mayconr.juoserver.game.session.player.MountService;
import com.github.mayconr.juoserver.game.session.world.WorldInternal;
import com.github.mayconr.juoserver.network.packet.DoubleClick;
import com.github.mayconr.juoserver.network.packet.ObjectInfo;
import com.github.mayconr.juoserver.network.packet.OpenPaperdoll;
import com.github.mayconr.juoserver.network.packet.SingleClickRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ClickService {

    private final UOPlayer player;
    private final WorldInternal worldInternal;
    private final SessionOutbound outbound;
    private final MountService mountService;
    private final PlayerItemService playerItemService;
    private final ItemUseService itemUseService;

    public void doubleClick(DoubleClick doubleClick) {
        final var serialId = doubleClick.getSerialId();

        if (doubleClick.isPaperdool() || worldInternal.isMobile(serialId)) {
            mobileClicked(doubleClick);
        } else {
            itemClicked(serialId);
        }
    }

    private void mobileClicked(DoubleClick doubleClick) {
        final var serialId = doubleClick.getSerialId();

        if (serialId == player.getSerialId()) {
            selfDoubleClick(doubleClick);
            return;
        }

        worldInternal.getMobileBySerialId(serialId)
                .ifPresent(this::otherMobileDoubleClick);
    }

    private void selfDoubleClick(DoubleClick doubleClick) {
        if (doubleClick.isPaperdool()) {
            openPaperdoll(doubleClick.getSerialId());
            return;
        }

        if (!player.isWarMode() && player.getEquippedItems().containsKey(Layer.MOUNT)) {
            mountService.handleUnmount();
        }
    }

    private void otherMobileDoubleClick(UOMobile mobile) {
        if (mobile instanceof UONpc npc && NpcType.MOUNT.equals(npc.getType())) {
            mountService.handleMount(npc);
        } else {
            openPaperdoll(mobile.getSerialId());
        }
    }

    private void openPaperdoll(int serialId) {
        worldInternal.getMobileBySerialId(serialId)
            .ifPresent(mobile->{
                outbound.writeAndFlush(new OpenPaperdoll(mobile, OpenPaperdoll.Flag.NORMAL));
            });
    }

    private void itemClicked(int serialId) {
        worldInternal.getItemBySerialId(serialId)
            .ifPresent(item->{
                // TODO check the item range

                if (item instanceof Container container) {
                    playerItemService.openContainer(container);
                    return;
                }

                if (player.isLayerAvailable(item.getLayer())) {
                    playerItemService.equipItem(item, item.getLayer());
                }
                // if the item is not a container, delegate the behavior
                itemUseService.use(new ItemUseContext(player, item, Trigger.DOUBLE_CLICK));
            });
    }

    public void singleClick(SingleClickRequest request) {
        final int serial = request.getSerialId();
        if (UOMobile.isMobile(serial)) {

        } else if (UOItem.isItem(serial)) {
            worldInternal.getItemBySerialId(serial)
                .ifPresent(item->{
                    outbound.writeAndFlush(new ObjectInfo(item));
                });
        }

    }

}
