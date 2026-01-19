package com.github.mayconr.juoserver.game.session.player;

import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.session.SessionOutbound;
import com.github.mayconr.juoserver.game.world.WorldService;

import com.github.mayconr.juoserver.network.packet.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
class DoubleClickService {

    private final UOPlayer player;
    private final WorldService worldService;
    private final SessionOutbound outbound;
    private final MountService mountService;

    public void handleDoubleClick(DoubleClick doubleClick) {
        final var serialId = doubleClick.getSerialId();

        if (doubleClick.isPaperdool() || worldService.isMobile(serialId)) {
            handleMobileDoubleClick(serialId);
        } else {
            handleItemDoubleClick(serialId);
        }
    }

    private void handleMobileDoubleClick(int serialId) {
        if (serialId == player.getSerialId()) {
            handleSelfDoubleClick(serialId);
            return;
        }

        worldService.findMobileBySerialId(serialId)
            .whenComplete((mobileOpt, throwable) -> {
                if (throwable != null) {
                    log.error("Unable to load mobile [{}]", serialId, throwable);
                }
            })
            .thenAccept(mobileOpt ->
                    mobileOpt.ifPresent(this::handleOtherMobileDoubleClick)
            );
    }

    private void handleSelfDoubleClick(int serialId) {
        if (!player.isWarMode() && player.getEquippedItems().containsKey(Layer.MOUNT)) {
            mountService.handleUnmount();
            return;
        }

        openPaperdoll(serialId);
    }

    private void handleOtherMobileDoubleClick(UOMobile mobile) {
        if (mobile instanceof UONpc npc && NpcType.MOUNT.equals(npc.getType())) {
            mountService.handleMount(npc);
        }

        openPaperdoll(mobile.getSerialId());
    }

    private void openPaperdoll(int serialId) {
        worldService
            .findMobileBySerialId(serialId)
            .whenComplete(
                    (mobOpt, ex) -> {
                        if (ex != null) {
                            log.error("Error to load mobile for serial {}", serialId, ex);
                            throw new MobileNotFoundException(serialId);
                        }
                        if (mobOpt.isEmpty()) {
                            log.debug("Mobile not found for serial {}", serialId);
                            throw new MobileNotFoundException(serialId);
                        }

                        final var mobile = mobOpt.get();

                        outbound.writeAndFlush(new OpenPaperdoll(mobile, OpenPaperdoll.Flag.NORMAL));
                    });
    }

    private void handleItemDoubleClick(int serialId) {
        worldService.findItemBySerialId(serialId)
            .whenComplete((itemOpt, throwable) -> {
                if (throwable != null) {
                    log.error("Unable to load item [{}]", serialId, throwable);
                }
            })
            .thenAccept(itemOpt ->
                    itemOpt.ifPresent(this::handleItemInteraction)
            );
    }

    private void handleItemInteraction(UOItem item) {
        if (item instanceof Container container) {
            openContainer(container);
            return;
        }

        moveItemToPlayer(item);
    }

    private void openContainer(Container container) {
        outbound.write(new DrawContainer(container));
        if (!container.getItemsInContainer().isEmpty()) {
            outbound.write(new AddMultipleItemsToContainer(
                    container,
                    container.getItemsInContainer()
            ));
        }
        outbound.flush();
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
