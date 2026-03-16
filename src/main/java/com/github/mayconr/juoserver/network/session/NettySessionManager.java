package com.github.mayconr.juoserver.network.session;

import com.github.mayconr.juoserver.JuoforgeConfiguration;
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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
public class NettySessionManager implements SessionManager {

    private final Set<PlayerSession> sessionMap = new HashSet<>();
    private final WorldInternal world;
    private final ChannelGroup channelGroup;
    private final JuoforgeConfiguration configuration;
    private final EventBus eventBus;

    record Event<T extends GameEvent>(Class<T> eventClass, EventHandler<T> handler) { }

    @Override
    public PlayerSession createSession(Channel channel) {
        final var session = new NettyPlayerSession(channel, channelGroup, configuration, eventBus, world);

        sessionMap.add(session);

        final var eventList = eventList(session);
        for (Event event : eventList) {
            eventBus.register(event.eventClass(), event.handler());
        }

        channel.closeFuture().addListener((future) -> {
            for (Event event : eventList) {
                eventBus.unregister(event.eventClass(), event.handler());
            }

            sessionMap.remove(session);

            eventBus.publish(new PlayerSessionClosed(session));
        });

        return session;
    }

    private List<Event<?>> eventList(NettyPlayerSession session) {
        return List.of(
                new Event<>(MobileMoved.class, session::onMobileMoved),
                new Event<>(MobileSpeech.class, session::onMobileSpeech),
                new Event<>(ItemEquipped.class, session::onItemEquipped),
                new Event<>(ItemUnequipped.class, session::onItemUnequipped),
                new Event<>(MobileEnteredLineOfSight.class, session::onEnteredLineOfSight),
                new Event<>(NpcCreated.class, session::onNpcCreated),
                new Event<>(MobileDeleted.class, session::onMobileDeleted),
                new Event<>(PlayerDeleted.class, session::onPlayerDeleted),
                new Event<>(SkillGained.class, session::onSkillGained),
                new Event<>(SkillGumpRequested.class, session::onSkillGumpRequested),
                new Event<>(StatusGumpRequested.class, session::onStatusGumpRequested),
                new Event<>(TooltipRequested.class, session::onTooltipRequested),
                new Event<>(ItemDroppedOnTheGround.class, session::onItemDroppedOnTheGround),
                new Event<>(ItemDroppedInContainer.class, session::onItemDroppedInContainer),
                new Event<>(ItemStacked.class, session::onItemStacked),
                new Event<>(GroundedItemCreated.class, session::onItemCreated),
                new Event<>(EquippedItemCreated.class, session::onItemCreated),
                new Event<>(ItemCreatedInContainer.class, session::onItemCreated),
                new Event<>(ItemDeleted.class, session::onItemDeleted),
                new Event<>(ItemUpdated.class, session::onItemUpdated),
                new Event<>(AnimationSent.class, session::onAnimationSent),
                new Event<>(PlayerLoggedIn.class, session::onPlayerLoggedIn),
                new Event<>(MessageSent.class, session::onMessageSent),
                new Event<>(TargetSent.class, session::onTargetSent),
                new Event<>(PaperdollOpened.class, session::onPaperdollOpened),
                new Event<>(ContainerOpened.class, session::onContainerOpened),
                new Event<>(SkillLocked.class, session::onSkillLocked),
                new Event<>(MobileStatusChanged.class, session::onMobileStatusChanged),
                new Event<>(PlayerStartAttack.class, session::onPlayerStartAttack),
                new Event<>(VendorSessionOpened.class, session::onVendorTradeSessionOpened),
                new Event<>(VitalsChanged.class, session::onVitalsChanged),
                new Event<>(GumpSent.class, session::onGumpSent),
                new Event<>(PlayerLoggedOut.class, session::onPlayerLoggedOut),
                new Event<>(MobileGoldChanged.class, session::onMobileGoldChanged)
        );
    }

}
