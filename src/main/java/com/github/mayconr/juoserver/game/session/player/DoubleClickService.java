package com.github.mayconr.juoserver.game.session.player;

import com.github.mayconr.juoserver.common.useitem.ItemUseContext;
import com.github.mayconr.juoserver.common.useitem.ItemUseService;
import com.github.mayconr.juoserver.common.useitem.Trigger;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.session.SessionOutbound;
import com.github.mayconr.juoserver.game.session.player.item.PlayerItemService;
import com.github.mayconr.juoserver.game.session.world.WorldSession;
import com.github.mayconr.juoserver.network.packet.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
class DoubleClickService {

    private final UOPlayer player;
    private final WorldSession worldSession;
    private final SessionOutbound outbound;
    private final MountService mountService;
    private final PlayerItemService playerItemService;
    private final ItemUseService itemUseService;

    public void handleDoubleClick(DoubleClick doubleClick) {
        final var serialId = doubleClick.getSerialId();

        if (doubleClick.isPaperdool() || worldSession.isMobile(serialId)) {
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

        worldSession.findMobileBySerialId(serialId)
            .thenAccept(this::otherMobileDoubleClick)
            .whenComplete((mobileOpt, throwable) -> {
                if (throwable != null) {
                    log.error("Unable to load mobile [{}]", serialId, throwable);
                }
            });
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
        worldSession
            .findMobileBySerialId(serialId)
            .whenComplete((mobile, throwable) -> {
                    if (throwable != null) {
                        log.error("Error to load mobile for serial {}", serialId, throwable);
                        throw new MobileNotFoundException(serialId);
                    }
                    outbound.writeAndFlush(new OpenPaperdoll(mobile, OpenPaperdoll.Flag.NORMAL));
                });
    }

    private void itemClicked(int serialId) {
        worldSession.findItemBySerialId(serialId)
            .thenAccept(item->{
                // TODO check the item range

                if (item instanceof Container container) {
                    playerItemService.openContainer(container);
                    return;
                }

                // if the item is not a container, delegate the behavior
                itemUseService.use(new ItemUseContext(player, item, Trigger.DOUBLE_CLICK));
            })
            .whenComplete((itemOpt, throwable) -> {
                if (throwable != null) {
                    log.error("Unable to load item [{}]", serialId, throwable);
                }
            });
    }

}
