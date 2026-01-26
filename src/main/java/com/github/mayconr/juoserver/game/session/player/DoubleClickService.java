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
            handleSelfDoubleClick(doubleClick);
            return;
        }

        worldSession.findMobileBySerialId(serialId)
            .thenAccept(mobileOpt ->
                    mobileOpt.ifPresent(this::handleOtherMobileDoubleClick)
            )
            .whenComplete((mobileOpt, throwable) -> {
                if (throwable != null) {
                    log.error("Unable to load mobile [{}]", serialId, throwable);
                }
            });
    }

    private void handleSelfDoubleClick(DoubleClick doubleClick) {
        if (doubleClick.isPaperdool()) {
            openPaperdoll(doubleClick.getSerialId());
            return;
        }

        if (!player.isWarMode() && player.getEquippedItems().containsKey(Layer.MOUNT)) {
            mountService.handleUnmount();
        }
    }

    private void handleOtherMobileDoubleClick(UOMobile mobile) {
        if (mobile instanceof UONpc npc && NpcType.MOUNT.equals(npc.getType())) {
            mountService.handleMount(npc);
        }

        openPaperdoll(mobile.getSerialId());
    }

    private void openPaperdoll(int serialId) {
        worldSession
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

    private void itemClicked(int serialId) {
        worldSession.findItemBySerialId(serialId)
            .thenAccept(itemOpt -> itemOpt.ifPresent(this::handleItemInteraction))
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
            .thenApply(items->{
                container.addItemsToContainer(items);
                return container;
            })
            .thenAccept(con->{
                outbound.write(new DrawContainer(con));
                if (!con.getItemsInContainer().isEmpty()) {
                    outbound.write(new AddMultipleItemsToContainer(con, con.getItemsInContainer()));
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
