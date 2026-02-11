package com.github.mayconr.juoserver.game.gump;

import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.player.SessionFanout;
import com.github.mayconr.juoserver.game.player.SessionOutbound;
import com.github.mayconr.juoserver.network.handler.AttributeKeys;
import com.github.mayconr.juoserver.network.packet.GumpSelection;
import com.github.mayconr.juoserver.network.packet.SendGumpDialog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Optional;
import java.util.Random;

@Slf4j
@RequiredArgsConstructor
public class DefaultGumpSystem implements GumpSystem, GumpSystemCallback {

    private final Random random = new Random();
    private final SessionFanout fanout;

    @Override
    public <T> void send(UOMobile mobile, DeclarativeGumpUI gumpUI, GumpHandler handler) {
        fanout.getSessionOutbound(mobile).ifPresent(outbound -> {
            final var gumpId = generateGumpId(outbound, (UOPlayer) mobile, handler);

            // Build the gump
            final var builder = new GumpBuilder();
            gumpUI.render(builder);
            final var built = builder.build();

            outbound.writeAndFlush(new SendGumpDialog(mobile, gumpId, 100, 100, built.layout, built.texts));
            log.info("Sent gump {} to player {}", gumpId, mobile.getName());
        });

    }

    private int generateGumpId(SessionOutbound outbound, UOPlayer player, GumpHandler handler) {
        final var attribute = outbound.attr().get(AttributeKeys.GUMP_IDS);
        final var idMap = Optional.ofNullable(attribute).orElseGet(HashMap::new);
        int gumpId = random.nextInt();
        while (idMap.containsKey(gumpId)) {
            gumpId = random.nextInt();
        }
        idMap.put(gumpId, new GumpContext(gumpId, player, System.currentTimeMillis(), handler));
        outbound.attr().set(AttributeKeys.GUMP_IDS, idMap);
        return gumpId;
    }

    @Override
    public void onGumpSelection(SessionOutbound outbound, GumpSelection gumpSelection) {
        log.debug(
                "Response received for GumpId {} on session {}",
                gumpSelection.getGumpId(),
                outbound);

        var map = outbound.attr().get(AttributeKeys.GUMP_IDS);
        if (map == null) {
            log.warn("Gump Aborted: Gump response without active gumps from session {}", outbound);
            return;
        }
        final var context = map.remove(gumpSelection.getGumpId());

        if (context.player().getSerialId() != gumpSelection.getSerialId()) {
            log.warn("Gump Aborted: Gump spoofing attempt for channel {}", outbound);
            return;
        }

        final var now = System.currentTimeMillis();
        if (now - context.createdAt() > 1000 * 60 * 2) { // TODO add to property
            map.remove(context.gumpId());
            log.warn("Gump Aborted: Expired gump {}", context.gumpId());
            return;
        }

        final var last = outbound.attr().get(AttributeKeys.LAST_GUMP_RESPONSE);
        if (last != null && now - last < 100) { // TODO add to property
            log.warn("Gump Aborted: spam attempt");
            return;
        }
        outbound.attr().set(AttributeKeys.LAST_GUMP_RESPONSE, now);

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
            log.warn("Replay or invalid gump {} for session {}", gumpSelection.getGumpId(), outbound);
            return;
        }
        handler.handle(context, gumpSelection);
    }
}
