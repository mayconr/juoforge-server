package com.github.mayconr.juoserver.game.core.gump;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import com.github.mayconr.juoserver.game.core.model.UOMobile;
import com.github.mayconr.juoserver.game.core.model.UOPlayer;
import com.github.mayconr.juoserver.game.packet.GumpSelection;
import com.github.mayconr.juoserver.game.packet.SendGumpDialog;
import com.github.mayconr.juoserver.game.packet.handler.AttributeKeys;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.util.AttributeKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class DefaultHandlerGumpSystem implements GumpSystem, GumpSystemCallback {

    private static final AttributeKey<Map<Integer, GumpContext>> GUMP_IDS =
            AttributeKey.valueOf(Map.class.getName());
    private static final AttributeKey<Long> LAST_GUMP_RESPONSE =
            AttributeKey.valueOf(Long.class.getName());
    private final ChannelGroup channelGroup;
    private final Random random = new Random();

    @Override
    public <T> void send(UOMobile mobile, DeclarativeGumpUI gumpUI, GumpHandler handler) {
        final var channel = getPlayerChannel((UOPlayer) mobile);
        final var gumpId = createGumpId(channel, (UOPlayer) mobile, handler);

        // Build the gump
        final var builder = new GumpBuilder();
        gumpUI.render(builder);
        final var built = builder.build();

        channel.writeAndFlush(
                new SendGumpDialog(mobile, gumpId, 100, 100, built.layout, built.texts));
        log.info("Sent gump {} to player {}", gumpId, mobile.getName());
    }

    private Channel getPlayerChannel(UOPlayer player) {
        return channelGroup.stream()
                .filter(
                        ch ->
                                player.equals(
                                        ch.attr(AttributeKeys.PLAYER_SESSION).get().getPlayer()))
                .findFirst()
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "Channel not found for mobile ["
                                                + player.getAccountId()
                                                + "]"));
    }

    private int createGumpId(Channel channel, UOPlayer player, GumpHandler handler) {
        final var attribute = channel.attr(GUMP_IDS);
        final var ids = Optional.ofNullable(attribute.get()).orElseGet(HashMap::new);
        int gumpId = random.nextInt();
        while (ids.containsKey(gumpId)) {
            gumpId = random.nextInt();
        }
        ids.put(
                gumpId,
                new GumpContext(gumpId, player, System.currentTimeMillis(), handler));
        attribute.set(ids);
        return gumpId;
    }

    @Override
    public void onGumpSelection(Channel channel, GumpSelection gumpSelection) {
        log.debug(
                "Response received for GumpId {} on channel {}",
                gumpSelection.getGumpId(),
                channel);

        var map = channel.attr(GUMP_IDS).get();
        if (map == null) {
            log.warn("Gump Aborted: Gump response without active gumps from channel {}", channel);
            return;
        }
        final var context = map.remove(gumpSelection.getGumpId());

        if (context.player().getSerialId() != gumpSelection.getSerialId()) {
            log.warn("Gump Aborted: Gump spoofing attempt for channel {}", channel);
            return;
        }

        final var now = System.currentTimeMillis();
        if (now - context.createdAt() > 1000 * 60 * 2) { // TODO add to property
            map.remove(context.gumpId());
            log.warn("Gump Aborted: Expired gump {}", context.gumpId());
            return;
        }

        final var last = channel.attr(LAST_GUMP_RESPONSE).get();
        if (last != null && now - last < 100) { // TODO add to property
            log.warn("Gump Aborted: spam attempt");
            return;
        }
        channel.attr(LAST_GUMP_RESPONSE).set(now);

        // Text Entry validation
        for (String text : gumpSelection.getTextEntries()) {
            if (text.length() > 200) { // TODO add to property
                log.warn("Gump Aborted: Text too long");
                return;
            }

            if (text.contains("\u0000")) {
                log.warn("Gump Aborted: Null byte in text");
                return;
            }
        }

        final var handler = context.handler();
        if (handler == null) {
            log.warn(
                    "Replay or invalid gump {} for channel {}", gumpSelection.getGumpId(), channel);
            return;
        }
        handler.handle(context, gumpSelection);
    }
}
