package com.github.mayconr.juoserver.game.player;

import com.github.mayconr.juoserver.game.model.event.*;
import com.github.mayconr.juoserver.game.world.WorldInternal;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.network.handler.AttributeKeys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RequiredArgsConstructor
public class DefaultSessionManager implements SessionManager {

    private final Map<Integer, PlayerSession> sessionMap = new ConcurrentHashMap<>();
    private final WorldInternal world;
    private final EventBus eventBus;

    @Override
    public void register(PlayerSession session) {
        final var serial = session.getPlayer().getSerialId();

        sessionMap.put(serial, session);

        final var outbound = session.getOutbound();

        outbound.attr().set(AttributeKeys.PLAYER_SESSION_KEY, session);
        registerEvents((DefaultPlayerSession) session);
        session.initialize(world, "");
        eventBus.publish(new PlayerSessionCreated(session));

        outbound.onChannelClosed(()->{
            unregisterEvents((DefaultPlayerSession) session);
            session.deactivate();
            sessionMap.remove(serial);
            eventBus.publish(new PlayerSessionClosed(session));

            log.info("Session closed for player [{}-{}]", serial, session.getPlayer().getName());
        });
    }

    private void registerEvents(DefaultPlayerSession session) {
        eventBus.register(MobileMoved.class, session::onMobileMoved);
        eventBus.register(MobileSpeech.class, session::onMobileSpeech);
        eventBus.register(ItemEquipped.class, session::onItemEquipped);
        eventBus.register(ItemUnequipped.class, session::onItemUnequipped);
        eventBus.register(MobileEnteredLineOfSight.class, session::onEnteredLineOfSight);
        eventBus.register(NpcCreated.class, session::onNpcCreated);
        eventBus.register(MobileDeleted.class, session::onMobileDeleted);
        eventBus.register(PlayerDeleted.class, session::onPlayerDeleted);
        eventBus.register(SkillGained.class, session::onSkillGained);
        eventBus.register(SkillGumpRequested.class, session::onSkillGumpRequested);
        eventBus.register(StatusGumpRequested.class, session::onStatusGumpRequested);
        eventBus.register(TooltipRequested.class, session::onTooltipRequested);
        eventBus.register(ItemDroppedOnTheGround.class, session::onItemDroppedOnTheGround);
        eventBus.register(ItemDroppedInContainer.class, session::onItemDroppedInContainer);
        eventBus.register(ItemStacked.class, session::onItemStacked);
        eventBus.register(GroundedItemCreated.class, session::onItemCreated);
        eventBus.register(EquippedItemCreated.class, session::onItemCreated);
        eventBus.register(ItemCreatedInContainer.class, session::onItemCreated);
        eventBus.register(ItemDeleted.class, session::onItemDeleted);
        eventBus.register(ItemUpdated.class, session::onItemUpdated);
        eventBus.register(AnimationSent.class, session::onAnimationSent);
        eventBus.register(PlayerLoggedIn.class, session::onPlayerLoggedIn);
        eventBus.register(MessageSent.class, session::onMessageSent);
        eventBus.register(TargetSent.class, session::onTargetSent);
        eventBus.register(PaperdollOpened.class, session::onPaperdollOpened);
        eventBus.register(ContainerOpened.class, session::onContainerOpened);
        eventBus.register(SkillLocked.class, session::onSkillLocked);
        eventBus.register(MobileStatusChanged.class, session::onMobileStatusChanged);
        eventBus.register(PlayerStartAttack.class, session::onPlayerStartAttack);
        eventBus.register(VendorSessionOpened.class, session::onVendorTradeSessionOpened);
        eventBus.register(VitalsChanged.class, session::onVitalsChanged);
        eventBus.register(GumpSent.class, session::onGumpSent);
        eventBus.register(PlayerLoggedOut.class, session::onPlayerLoggedOut);
        eventBus.register(MobileGoldChanged.class, session::onMobileGoldChanged);
    }

    private void unregisterEvents(DefaultPlayerSession session) {
        eventBus.unregister(MobileMoved.class, session::onMobileMoved);
        eventBus.unregister(MobileSpeech.class, session::onMobileSpeech);
        eventBus.unregister(ItemEquipped.class, session::onItemEquipped);
        eventBus.unregister(ItemUnequipped.class, session::onItemUnequipped);
        eventBus.unregister(MobileEnteredLineOfSight.class, session::onEnteredLineOfSight);
        eventBus.unregister(NpcCreated.class, session::onNpcCreated);
        eventBus.unregister(MobileDeleted.class, session::onMobileDeleted);
        eventBus.unregister(PlayerDeleted.class, session::onPlayerDeleted);
        eventBus.unregister(SkillGained.class, session::onSkillGained);
        eventBus.unregister(SkillGumpRequested.class, session::onSkillGumpRequested);
        eventBus.unregister(StatusGumpRequested.class, session::onStatusGumpRequested);
        eventBus.unregister(TooltipRequested.class, session::onTooltipRequested);
        eventBus.unregister(ItemDroppedOnTheGround.class, session::onItemDroppedOnTheGround);
        eventBus.unregister(ItemDroppedInContainer.class, session::onItemDroppedInContainer);
        eventBus.unregister(ItemStacked.class, session::onItemStacked);
        eventBus.unregister(GroundedItemCreated.class, session::onItemCreated);
        eventBus.unregister(EquippedItemCreated.class, session::onItemCreated);
        eventBus.unregister(ItemCreatedInContainer.class, session::onItemCreated);
        eventBus.unregister(ItemDeleted.class, session::onItemDeleted);
        eventBus.unregister(ItemUpdated.class, session::onItemUpdated);
        eventBus.unregister(AnimationSent.class, session::onAnimationSent);
        eventBus.unregister(PlayerLoggedIn.class, session::onPlayerLoggedIn);
        eventBus.unregister(MessageSent.class, session::onMessageSent);
        eventBus.unregister(TargetSent.class, session::onTargetSent);
        eventBus.unregister(PaperdollOpened.class, session::onPaperdollOpened);
        eventBus.unregister(ContainerOpened.class, session::onContainerOpened);
        eventBus.unregister(SkillLocked.class, session::onSkillLocked);
        eventBus.unregister(MobileStatusChanged.class, session::onMobileStatusChanged);
        eventBus.unregister(PlayerStartAttack.class, session::onPlayerStartAttack);
        eventBus.unregister(VendorSessionOpened.class, session::onVendorTradeSessionOpened);
        eventBus.unregister(VitalsChanged.class, session::onVitalsChanged);
        eventBus.unregister(GumpSent.class, session::onGumpSent);
        eventBus.unregister(PlayerLoggedOut.class, session::onPlayerLoggedOut);
        eventBus.unregister(MobileGoldChanged.class, session::onMobileGoldChanged);
    }
}
