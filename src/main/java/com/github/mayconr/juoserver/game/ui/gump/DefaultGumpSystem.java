package com.github.mayconr.juoserver.game.ui.gump;

import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.event.GumpSent;
import com.github.mayconr.juoserver.game.model.event.GumpExpired;
import com.github.mayconr.juoserver.game.GamePlaySettings;
import com.github.mayconr.juoserver.infrastructure.i18n.MessageLocalizer;
import com.github.mayconr.juoserver.game.model.event.message.PlainTextMessageContent;
import com.github.mayconr.juoserver.game.model.event.message.LocalizedMessageContent;
import com.github.mayconr.juoserver.network.packet.GumpSelection;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongSupplier;
import java.util.Map;
import java.util.Random;
import java.util.Objects;

@Slf4j
public class DefaultGumpSystem implements GumpSystem {

    public static final String PLAYER_GUMP_ID = "player.gumpId";
    public static final String PLAYER_LAST_GUMP_RESPONSE = "player.lastGumpResponse";
    private final Random random = new Random();
    private final EventBus eventBus;
    private final MessageLocalizer localizer;
    private final double defaultTimeoutSeconds;
    private final LongSupplier clock;
    private final Set<UOPlayer> pendingPlayers = ConcurrentHashMap.newKeySet();

    public DefaultGumpSystem(EventBus eventBus, GamePlaySettings.Ui settings, MessageLocalizer localizer) {
        this(eventBus, settings, localizer, System::currentTimeMillis);
    }

    DefaultGumpSystem(EventBus eventBus, GamePlaySettings.Ui settings, MessageLocalizer localizer, LongSupplier clock) {
        this.localizer = Objects.requireNonNull(localizer, "Message localizer is required");
        this.eventBus = Objects.requireNonNull(eventBus, "Event bus is required");
        this.defaultTimeoutSeconds = Objects.requireNonNull(settings, "UI settings are required")
                .defaultGumpTimeoutSeconds();
        this.clock = Objects.requireNonNull(clock, "Clock is required");
    }

    @Override
    public void update(double delta) {
        long now = clock.getAsLong();
        var expired = new ArrayList<GumpExpired>();
        for (var player : pendingPlayers) {
            synchronized (player) {
                var map = pendingGumps(player);
                if (map != null) {
                    var iterator = map.values().iterator();
                    while (iterator.hasNext()) {
                        var context = iterator.next();
                        if (context.isExpired(now)) {
                            iterator.remove();
                            expired.add(new GumpExpired(player, context.gumpId()));
                        }
                    }
                }
                releasePlayerIfEmpty(player, map);
            }
        }
        // Publish after removing contexts and releasing player locks, allowing listener reentry.
        expired.forEach(eventBus::publish);
    }

    @SuppressWarnings("unchecked")
    private Map<Integer, GumpContext> pendingGumps(UOPlayer player) {
        return (Map<Integer, GumpContext>) player.runtimeAttributes().get(PLAYER_GUMP_ID);
    }

    private void releasePlayerIfEmpty(UOPlayer player, Map<Integer, GumpContext> map) {
        if (map == null || map.isEmpty()) {
            player.runtimeAttributes().remove(PLAYER_GUMP_ID);
            pendingPlayers.remove(player);
        }
    }

    @Override
    public void send(UOPlayer player, DeclarativeGumpUI gumpUI, GumpHandler handler) {
        send(player, gumpUI, handler, defaultTimeoutSeconds);
    }

    @Override
    public void send(UOPlayer player, DeclarativeGumpUI gumpUI, GumpHandler handler, double expirationSeconds) {
        if (!Double.isFinite(expirationSeconds) || expirationSeconds <= 0
                || expirationSeconds >= Long.MAX_VALUE / 1000.0) {
            throw new IllegalArgumentException("Gump expiration seconds must be finite, positive and fit in milliseconds");
        }
        long timeoutMillis = (long) Math.ceil(expirationSeconds * 1000.0);
        Objects.requireNonNull(player, "Player is required");
        Objects.requireNonNull(gumpUI, "Gump UI is required");
        Objects.requireNonNull(handler, "Gump handler is required");

        // Build the gump
        final var builder = new GumpBuilder();
        var locale = player.getLocale();
        // Rendering serializes access to the definition's resolved text and layout coordinates.
        gumpUI.render(builder, content -> switch (content) {
            case PlainTextMessageContent plain -> plain.text();
            case LocalizedMessageContent localized -> localizer.localize(localized, locale);
        });

        var built = builder.build();
        final var gumpId = generateGumpId(player, handler, timeoutMillis);
        eventBus.publish(new GumpSent(player, built, gumpId));
        log.debug("Sent gump {} to player {}", gumpId, player.getName());
    }

    private int generateGumpId(UOPlayer player, GumpHandler handler, long timeoutMillis) {
        synchronized (player) {
            var attributes = player.runtimeAttributes();
            final var gumpIds = attributes.getOrDefault(PLAYER_GUMP_ID, new HashMap<>());
            int gumpId = random.nextInt();
            while (gumpIds.containsKey(gumpId)) {
                gumpId = random.nextInt();
            }
            gumpIds.put(gumpId, new GumpContext(gumpId, player, clock.getAsLong(), timeoutMillis, handler));

            attributes.set(PLAYER_GUMP_ID, gumpIds);
            pendingPlayers.add(player);

            return gumpId;
        }
    }

    @Override
    public void onGumpSelection(UOPlayer player, GumpSelection gumpSelection) {
        if (player == null) {
            log.warn("Ignoring gump response without an active player");
            return;
        }
        Objects.requireNonNull(gumpSelection, "Gump selection is required");
        final GumpContext context;
        synchronized (player) {
            var map = pendingGumps(player);
            var pending = map == null ? null : map.get(gumpSelection.getGumpId());
            if (pending == null) {
                log.warn("Unknown or repeated gump {} for player {}", gumpSelection.getGumpId(), player.getName());
                return;
            }
            if (pending.player().getSerialId() != gumpSelection.getSerialId()) {
                log.warn("Gump response has an invalid player serial for player {}", player.getName());
                return;
            }
            final var now = clock.getAsLong();
            if (pending.isExpired(now)) {
                // Leave removal and notification to the game loop update.
                log.warn("Expired gump {}", pending.gumpId());
                return;
            }
            final var last = player.runtimeAttributes().getOrDefault(PLAYER_LAST_GUMP_RESPONSE, 0L);
            if (last != null && now - last < 100) { // TODO add to property
                log.warn("Gump response rate exceeded for player {}", player.getName());
                return;
            }
            for (String text : gumpSelection.getTextEntries()) {
                if (text.length() > GumpSelection.MAX_TEXT_LENGTH || text.indexOf('\0') >= 0) {
                    log.warn("Invalid gump text for player {}", player.getName());
                    return;
                }
            }
            context = map.remove(pending.gumpId());
            releasePlayerIfEmpty(player, map);
            player.runtimeAttributes().set(PLAYER_LAST_GUMP_RESPONSE, now);
        }
        // Consume once under the shared player lock; execute user callbacks outside it.
        context.handler().handle(context, gumpSelection);
    }
}
