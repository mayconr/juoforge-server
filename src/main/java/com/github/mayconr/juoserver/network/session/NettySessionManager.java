package com.github.mayconr.juoserver.network.session;

import com.github.mayconr.juoserver.game.GamePlaySettings;
import com.github.mayconr.juoserver.game.model.event.AnimationSent;
import com.github.mayconr.juoserver.game.model.event.CombatOccurring;
import com.github.mayconr.juoserver.game.model.event.CombatStarted;
import com.github.mayconr.juoserver.game.model.event.ContainerOpenedEvent;
import com.github.mayconr.juoserver.game.model.event.ContextMenuRequested;
import com.github.mayconr.juoserver.game.model.event.EquippedItemCreated;
import com.github.mayconr.juoserver.game.model.event.GroundedItemCreated;
import com.github.mayconr.juoserver.game.model.event.GumpExpired;
import com.github.mayconr.juoserver.game.model.event.GumpSent;
import com.github.mayconr.juoserver.game.model.event.ItemCreatedInContainer;
import com.github.mayconr.juoserver.game.model.event.ItemDeleted;
import com.github.mayconr.juoserver.game.model.event.ItemDroppedOnTheGround;
import com.github.mayconr.juoserver.game.model.event.ItemEquipped;
import com.github.mayconr.juoserver.game.model.event.ItemStacked;
import com.github.mayconr.juoserver.game.model.event.ItemUnequipped;
import com.github.mayconr.juoserver.game.model.event.ItemUpdated;
import com.github.mayconr.juoserver.game.model.event.MobileDamagedEvent;
import com.github.mayconr.juoserver.game.model.event.MobileDeathEvent;
import com.github.mayconr.juoserver.game.model.event.MobileEnteredLineOfSight;
import com.github.mayconr.juoserver.game.model.event.MobileMoveRejected;
import com.github.mayconr.juoserver.game.model.event.MobileMoveResync;
import com.github.mayconr.juoserver.game.model.event.MobileMoved;
import com.github.mayconr.juoserver.game.model.event.MobileResurrectEvent;
import com.github.mayconr.juoserver.game.model.event.MobileSpeech;
import com.github.mayconr.juoserver.game.model.event.MobileStatusChanged;
import com.github.mayconr.juoserver.game.model.event.NpcCreated;
import com.github.mayconr.juoserver.game.model.event.NpcRemoved;
import com.github.mayconr.juoserver.game.model.event.ObjectLabelRequested;
import com.github.mayconr.juoserver.game.model.event.PaperdollOpened;
import com.github.mayconr.juoserver.game.model.event.PlayerDeleted;
import com.github.mayconr.juoserver.game.model.event.PlayerLoggedIn;
import com.github.mayconr.juoserver.game.model.event.PlayerLoggedOut;
import com.github.mayconr.juoserver.game.model.event.PlayerSessionClosed;
import com.github.mayconr.juoserver.game.model.event.RawPacketSent;
import com.github.mayconr.juoserver.game.model.event.RawPacketsSent;
import com.github.mayconr.juoserver.game.model.event.SkillGained;
import com.github.mayconr.juoserver.game.model.event.SkillGumpRequested;
import com.github.mayconr.juoserver.game.model.event.SkillLocked;
import com.github.mayconr.juoserver.game.model.event.SkillUpdated;
import com.github.mayconr.juoserver.game.model.event.SpellBookOpened;
import com.github.mayconr.juoserver.game.model.event.StatusGumpRequested;
import com.github.mayconr.juoserver.game.model.event.TargetSent;
import com.github.mayconr.juoserver.game.model.event.TooltipRequested;
import com.github.mayconr.juoserver.game.model.event.VendorSessionOpened;
import com.github.mayconr.juoserver.game.model.event.message.MessageSent;
import com.github.mayconr.juoserver.game.world.WorldInternal;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventHandler;
import com.github.mayconr.juoserver.infrastructure.eventbus.GameEvent;
import com.github.mayconr.juoserver.infrastructure.i18n.MessageLocalizer;
import com.github.mayconr.juoserver.infrastructure.storage.WorldStorage;
import com.github.mayconr.juoserver.network.session.handler.CombatEventHandler;
import com.github.mayconr.juoserver.network.session.handler.ItemEventHandler;
import com.github.mayconr.juoserver.network.session.handler.PlayerInterfaceHandler;
import com.github.mayconr.juoserver.network.session.handler.WorldViewHandler;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class NettySessionManager implements SessionManager {

    private final Set<PlayerSession> sessionMap = ConcurrentHashMap.newKeySet();
    private final WorldInternal world;
    private final ChannelGroup channelGroup;
    private final GamePlaySettings settings;
    private final EventBus eventBus;
    @NonNull
    private final MessageLocalizer localizer;
    private final WorldStorage storage;

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
        final var session = new NettyPlayerSession(packetWriter, settings, eventBus, world, storage);

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
        var ui = new PlayerInterfaceHandler(view, packetWriter, world, localizer);
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
                new Event<>(ContextMenuRequested.class, ui::onContextMenuRequested),
                new Event<>(GumpSent.class, ui::onGumpSent),
                new Event<>(GumpExpired.class, ui::onGumpExpired),
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
