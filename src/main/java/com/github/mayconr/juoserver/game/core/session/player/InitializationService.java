package com.github.mayconr.juoserver.game.core.session.player;

import com.github.mayconr.juoserver.game.core.event.EventBus;
import com.github.mayconr.juoserver.game.core.event.PlayerSessionStarted;
import com.github.mayconr.juoserver.game.core.model.Season;
import com.github.mayconr.juoserver.game.core.model.UOItem;
import com.github.mayconr.juoserver.game.core.model.UOMobile;
import com.github.mayconr.juoserver.game.core.session.SessionFanout;
import com.github.mayconr.juoserver.game.core.session.SessionOutbound;
import com.github.mayconr.juoserver.game.packet.*;
import com.github.mayconr.juoserver.game.server.Future;
import com.github.mayconr.juoserver.game.storage.WorldService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
class InitializationService {

    private final UOMobile mobile;
    private final EventBus eventBus;
    private final WorldService worldService;
    private final SessionOutbound outbound;
    private final SessionFanout fanout;

    public void initialize(PlayerSession session, String clientVersion) {
        outbound.write(new LoginConfirm(mobile, 7168, 4096));
        outbound.write(new SeasonalInformation(Season.Summer, true));

        Future.fire(worldService.getMobilesInRange(mobile)
            .thenAccept(mobiles->{
                drawMobiles(mobiles);
                Future.fire(worldService.getItemsInRange(mobile)
                    .thenAccept(items->{
                        drawItems(items);
                        finalizeLogin(session);
                    }));
            })
        );
    }

    private void drawMobiles(List<UOMobile> mobiles) {
        mobiles.stream()
                .filter(someone -> !someone.equals(mobile))
                .forEach(someone -> outbound.write(new DrawMobile(someone)));
    }

    private void drawItems(List<UOItem> items) {
        items.forEach(item -> outbound.write(new ObjectInfo(item)));
    }

    private void finalizeLogin(PlayerSession session) {
        outbound.write(new DrawGamePlayer(mobile));
        outbound.write(new DrawMobile(mobile));
        outbound.write(new StatusBarInfo(mobile));
        outbound.write(new LoginComplete());
        outbound.flush();

        fanout.writeAndFlush(new DrawMobile(mobile));

        log.info("Session initialized for {}", mobile);

        eventBus.publish(new PlayerSessionStarted(session));
    }
}
