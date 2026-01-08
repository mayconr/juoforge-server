package com.github.mayconr.juoserver.game.core.session.player;

import com.github.mayconr.juoserver.game.core.database.Database;
import com.github.mayconr.juoserver.game.core.model.*;
import com.github.mayconr.juoserver.game.packet.*;

import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
class DoubleClickService {

    private final UOPlayer player;
    private final Database database;
    private final ChannelHandlerContext ctx;
    private final MountService mountService;

    public void handleDoubleClick(DoubleClick doubleClick) {
        final var serialId = doubleClick.getSerialId();

        if (doubleClick.isPaperdool() || database.isMobile(serialId)) {
            handleMobileDoubleClick(serialId);
        } else {
            handleItemDoubleClick(serialId);
        }
    }

    private void handleMobileDoubleClick(int serialId) {
        if (serialId == player.getSerialId()) {
            if (!player.isWarMode() && player.getEquippedItems().containsKey(Layer.MOUNT)) {
                mountService.handleUnmount();
            } else {
                openPaperdoll(serialId);
            }
        } else {
            final var otherMobile =
                    database.getMobileSerialId(serialId)
                            .orElseThrow(() -> new MobileNotFoundException(serialId));
            if (otherMobile instanceof UONpc npc) {
                if (NpcType.MOUNT.equals(npc.getType())) {
                    mountService.handleMount(npc);
                }
            }
            openPaperdoll(serialId);
        }
    }

    private void openPaperdoll(int serialId) {
        final var mobile =
                database.getMobileSerialId(serialId)
                        .orElseThrow(() -> new MobileNotFoundException(serialId));

        if (isHumanNpc(mobile) || mobile instanceof UOPlayer) {
            ctx.writeAndFlush(new OpenPaperdoll(mobile, OpenPaperdoll.Flag.NORMAL));
        }
    }

    private boolean isHumanNpc(Object mobile) {
        return mobile instanceof UONpc npc && NpcType.HUMAN.equals(npc.getType());
    }

    private void handleItemDoubleClick(int serialId) {
        final var item =
                database.getItemBySerialId(serialId)
                        .orElseThrow(() -> new ItemNotFoundException(serialId));

        if (item instanceof Container container) {
            ctx.write(new DrawContainer(container));

            if (!container.getItemsInContainer().isEmpty()) {
                ctx.write(
                        new AddMultipleItemsToContainer(
                                container, container.getItemsInContainer()));
            }

            ctx.flush();
        } else {
            player.addItemToContainer(item);
            ctx.writeAndFlush(new AddItemToContainer(player, item));
            log.info(
                    "Item [{}-{}] added to container [{}-{}]",
                    item.getSerialId(),
                    item.getName(),
                    player.getSerialId(),
                    player.getName());
        }
    }
}
