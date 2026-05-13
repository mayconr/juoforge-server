package com.github.mayconr.juoserver.game.ui;

import com.github.mayconr.juoserver.game.item.trigger.ItemUseContext;
import com.github.mayconr.juoserver.game.item.trigger.ItemUseService;
import com.github.mayconr.juoserver.game.item.trigger.Trigger;
import com.github.mayconr.juoserver.game.model.Container;
import com.github.mayconr.juoserver.game.model.Layer;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.event.ContainerOpenedEvent;
import com.github.mayconr.juoserver.game.model.event.PaperdollOpened;
import com.github.mayconr.juoserver.game.model.policy.DoubleClickPolicy;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.policy.PolicyService;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import com.github.mayconr.juoserver.network.packet.DoubleClick;
import com.github.mayconr.juoserver.network.packet.OpenPaperdoll;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class DoubleClickHandler {

    private final EventBus eventBus;
    private final RealmStorage storage;
    private final ItemUseService itemUseService;
    private final PolicyService policyService;

    public void doubleClick(UOPlayer player, DoubleClick doubleClick) {
        final var result = policyService.evaluate(DoubleClickPolicy.class, new DoubleClickPolicy(player, doubleClick.getSerialId()));
        if (!result.allowed()) {
            log.debug("doubleClick aborted by policy. Reason: {}", result.reason());
            return;
        }

        final var serialId = doubleClick.getSerialId();

        if (doubleClick.isPaperdool() || UOMobile.isMobile(serialId)) {
            mobileClicked(player, doubleClick);
        } else {
            itemClicked(player, serialId);
        }
    }

    private void mobileClicked(UOPlayer player, DoubleClick doubleClick) {
        final var serialId = doubleClick.getSerialId();

        if (serialId == player.getSerialId()) {
            selfDoubleClick(player, doubleClick);
            return;
        }

        storage.getMobile(serialId)
                .ifPresent(mobile -> otherMobileDoubleClick(player, mobile));
    }

    private void selfDoubleClick(UOPlayer player, DoubleClick doubleClick) {
        if (doubleClick.isPaperdool()) {
            eventBus.publish(new PaperdollOpened(player, player, OpenPaperdoll.Flag.NORMAL));
            return;
        }

        if (!player.isWarMode() && player.getEquippedItems().containsKey(Layer.MOUNT)) {
            //mountService.handleUnmount();
        }
    }

    private void otherMobileDoubleClick(UOPlayer player, UOMobile mobile) {
        eventBus.publish(new PaperdollOpened(player, mobile, OpenPaperdoll.Flag.NORMAL));
    }

    private void itemClicked(UOPlayer player, int serialId) {
        storage.getItem(serialId)
            .ifPresent(item->{
                // TODO check the item range

                if (item instanceof Container container) {
                    openContainer(player, container);
                    return;
                }

                // if the item is not a container, delegate the behavior
                itemUseService.use(new ItemUseContext(player, item, Trigger.DOUBLE_CLICK));
            });
    }

    private void openContainer(UOPlayer player, Container container) {
        storage.loadContainerItems(container)
            .thenAccept(containerItems -> {
                container.addItemsToContainer(containerItems);
                eventBus.publish(new ContainerOpenedEvent(player, container));
            });
    }
}
