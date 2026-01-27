package com.github.mayconr.juoserver.game.session.player;

import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.session.SessionOutbound;
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
            .thenAccept(this::handleItemInteraction)
            .whenComplete((itemOpt, throwable) -> {
                if (throwable != null) {
                    log.error("Unable to load item [{}]", serialId, throwable);
                }
            });
    }

    private void handleItemInteraction(UOItem item) {
        if (item instanceof Container container) {
            openContainer(container);
            return;
        }

        moveItemToPlayer(item);
    }

    private void openContainer(Container container) {
        worldSession.loadContainerItems(container)
            .thenAccept(items->{
                container.addItemsToContainer(items);
                outbound.write(new DrawContainer(container));
                if (!container.getItemsInContainer().isEmpty()) {
                    outbound.write(new AddMultipleItemsToContainer(container, container.getItemsInContainer()));
                }
                outbound.flush();
            })
            .whenComplete(((unused, throwable) -> {
                if (throwable != null) {
                    log.error("Unable to open container [{}]", container.getId(), throwable);
                }
            }));

    }

    private void moveItemToPlayer(UOItem item) {
        player.addItemToContainer(item);

        outbound.writeAndFlush(new AddItemToContainer(player, item));

        log.info(
                "Item [{}-{}] added to container [{}-{}]",
                item.getSerialId(),
                item.getName(),
                player.getSerialId(),
                player.getName()
        );
    }
}
