package com.github.mayconr.juoserver.game.world.module.ui.gump;

import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.event.GumpSent;
import com.github.mayconr.juoserver.network.packet.GumpSelection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@RequiredArgsConstructor
public class DefaultGumpSystem implements GumpSystem {

    public static final String PLAYER_GUMP_ID = "player.gumpId";
    public static final String PLAYER_LAST_GUMP_RESPONSE = "player.lastGumpResponse";
    private final Random random = new Random();
    private final EventBus eventBus;

    @Override
    public void send(UOPlayer player, DeclarativeGumpUI gumpUI, GumpHandler handler) {
        final var gumpId = generateGumpId(player, handler);

        // Build the gump
        final var builder = new GumpBuilder();
        gumpUI.render(builder);

        eventBus.publish(new GumpSent(player, builder.build(), gumpId));
        log.info("Sent gump {} to player {}", gumpId, player.getName());
    }

    private int generateGumpId(UOPlayer player, GumpHandler handler) {
        ReentrantLock lock = new ReentrantLock();
        if (lock.tryLock()) {
            final var gumpIds = player.getRuntimeAttribute(PLAYER_GUMP_ID, new HashMap<>());
            int gumpId = random.nextInt();
            while (gumpIds.containsKey(gumpId)) {
                gumpId = random.nextInt();
            }
            gumpIds.put(gumpId, new GumpContext(gumpId, player, System.currentTimeMillis(), handler));

            player.setRuntimeAttribute(PLAYER_GUMP_ID, gumpIds);

            return gumpId;
        }
        throw new RuntimeException("Unable to generate gump id for player " + player.getName());
    }

    @Override
    public void onGumpSelection(UOPlayer player, GumpSelection gumpSelection) {
        log.debug(
                "Response received for GumpId {} for player {}",
                gumpSelection.getGumpId(),
                player.getName());

        var map = (Map<Integer, GumpContext>)player.getRuntimeAttribute(PLAYER_GUMP_ID);
        if (map == null) {
            log.warn("Gump Aborted: Gump response without active gumps for player {}", player.getName());
            return;
        }
        final var context = map.remove(gumpSelection.getGumpId());

        if (context.player().getSerialId() != gumpSelection.getSerialId()) {
            log.warn("Gump Aborted: Gump spoofing attempt for player {}", player.getName());
            return;
        }

        final var now = System.currentTimeMillis();
        if (now - context.createdAt() > 1000 * 60 * 2) { // TODO add to property
            map.remove(context.gumpId());
            log.warn("Gump Aborted: Expired gump {}", context.gumpId());
            return;
        }

        final var last = player.getRuntimeAttribute(PLAYER_LAST_GUMP_RESPONSE, 0L);
        if (last != null && now - last < 100) { // TODO add to property
            log.warn("Gump Aborted: spam attempt");
            return;
        }
        player.setRuntimeAttribute(PLAYER_LAST_GUMP_RESPONSE, now);

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
            log.warn("Replay or invalid gump {} for player {}", gumpSelection.getGumpId(), player.getName());
            return;
        }
        handler.handle(context, gumpSelection);
    }
}
