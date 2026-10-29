package com.github.mayconr.juoserver.network.session;

import com.github.mayconr.juoserver.game.GamePlaySettings;
import com.github.mayconr.juoserver.network.session.handler.CombatEventHandler;
import com.github.mayconr.juoserver.network.session.handler.ItemEventHandler;
import com.github.mayconr.juoserver.network.session.handler.PlayerInterfaceHandler;
import com.github.mayconr.juoserver.network.session.handler.WorldViewHandler;
import com.github.mayconr.juoserver.game.model.event.*;
import com.github.mayconr.juoserver.game.model.event.message.MessageSent;
import com.github.mayconr.juoserver.game.world.WorldInternal;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventHandler;
import com.github.mayconr.juoserver.infrastructure.eventbus.GameEvent;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
public class NettySessionManager implements SessionManager {

    private final Set<PlayerSession> sessionMap = ConcurrentHashMap.newKeySet();
    private final WorldInternal world;
    private final ChannelGroup channelGroup;
    private final GamePlaySettings settings;
    private final EventBus eventBus;

    record Event<T extends GameEvent>(Class<T> eventClass, EventHandler<T> handler) {
        Event<T> forSession(NettyPlayerSession session, SessionPacketWriter writer) {
            return new Event<>(eventClass, event -> {
                if (!session.isActive()) {
                    return;
                }
                writer.enqueue(packets -> {
                    synchronized (session) {
                        if (session.isActive()) {
                            handler.handle(event);
                        }
                    }
                });
            });
        }
    }

    @Override
    public PlayerSession createSession(Channel channel) {
        final var packetWriter = new SessionPacketWriter(channel, channelGroup);
        final var session = new NettyPlayerSession(packetWriter, settings, eventBus, world);

        sessionMap.add(session);

        final var eventList = eventList(session, packetWriter).stream()
                .map(event -> event.forSession(session, packetWriter)).toList();
        for (Event event : eventList) {
            eventBus.register(event.eventClass(), event.handler());
        }

        channel.closeFuture().addListener((future) -> {
            session.disconnect();
            for (Event event : eventList) {
                eventBus.unregister(event.eventClass(), event.handler());
            }

            sessionMap.remove(session);

            eventBus.publish(new PlayerSessionClosed(session));
        });

        return session;
    }

    private List<Event<?>> eventList(NettyPlayerSession session, SessionPacketWriter packetWriter) {
        var view = new SessionView(session::getPlayer, session::getLocale, settings);
        var worldView = new WorldViewHandler(view, packetWriter, world, settings);
        var items = new ItemEventHandler(view, packetWriter, world);
        var ui = new PlayerInterfaceHandler(view, packetWriter, world);
        var combat = new CombatEventHandler(view, packetWriter, world);
        return List.of(
                new Event<>(MobileMoved.class, worldView::onMobileMoved),
                new Event<>(MobileMoveRejected.class, worldView::onMobileMoveRejected),
                new Event<>(MobileMoveResync.class, worldView::onMobileResynced),
                new Event<>(MobileSpeech.class, ui::onMobileSpeech),
                new Event<>(ItemEquipped.class, items::onItemEquipped),
                new Event<>(ItemUnequipped.class, items::onItemUnequipped),
                new Event<>(MobileEnteredLineOfSight.class, worldView::onEnteredLineOfSight),
                new Event<>(NpcCreated.class, worldView::onNpcCreated),
                new Event<>(NpcRemoved.class, worldView::onMobileDeleted),
                new Event<>(PlayerDeleted.class, worldView::onPlayerDeleted),
                new Event<>(SkillGained.class, ui::onSkillGained),
                new Event<>(SkillUpdated.class, ui::onSkillUpdated),
                new Event<>(SkillGumpRequested.class, ui::onSkillGumpRequested),
                new Event<>(StatusGumpRequested.class, ui::onStatusGumpRequested),
                new Event<>(TooltipRequested.class, ui::onTooltipRequested),
                new Event<>(ObjectLabelRequested.class, ui::onObjectLabelRequested),
                new Event<>(ItemDroppedOnTheGround.class, items::onItemDroppedOnTheGround),
                new Event<>(ItemStacked.class, items::onItemStacked),
                new Event<>(GroundedItemCreated.class, items::onItemCreated),
                new Event<>(EquippedItemCreated.class, items::onEquippedItemCreated),
                new Event<>(ItemCreatedInContainer.class, items::onItemCreatedInContainer),
                new Event<>(ItemDeleted.class, items::onItemDeleted),
                new Event<>(ItemUpdated.class, items::onItemUpdated),
                new Event<>(AnimationSent.class, combat::onAnimationSent),
                new Event<>(PlayerLoggedIn.class, worldView::onPlayerLoggedIn),
                new Event<>(MessageSent.class, ui::onMessageSent),
                new Event<>(TargetSent.class, ui::onTargetSent),
                new Event<>(PaperdollOpened.class, ui::onPaperdollOpened),
                new Event<>(ContainerOpenedEvent.class, ui::onContainerOpened),
                new Event<>(SkillLocked.class, ui::onSkillLocked),
                new Event<>(MobileStatusChanged.class, combat::onMobileStatusChanged),
                new Event<>(VendorSessionOpened.class, ui::onVendorTradeSessionOpened),
                new Event<>(GumpSent.class, ui::onGumpSent),
                new Event<>(SpellBookOpened.class, ui::onSpellBookOpened),
                new Event<>(RawPacketSent.class, ui::onRawPacketSent),
                new Event<>(RawPacketsSent.class, ui::onRawPacketsSent),
                new Event<>(PlayerLoggedOut.class, worldView::onPlayerLoggedOut),
                new Event<>(MobileDamagedEvent.class, combat::onMobileDamaged),
                new Event<>(MobileDeathEvent.class, combat::onMobileDeath),
                new Event<>(MobileResurrectEvent.class, combat::onMobileResurrect),
                new Event<>(CombatStarted.class, combat::onCombatStarted),
                new Event<>(CombatOccurring.class, combat::onCombatOccurring)
        );
    }

}
