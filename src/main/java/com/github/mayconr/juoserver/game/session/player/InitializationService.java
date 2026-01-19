package com.github.mayconr.juoserver.game.session.player;

import com.github.mayconr.juoserver.common.event.EventBus;
import com.github.mayconr.juoserver.common.event.PlayerSessionStarted;
import com.github.mayconr.juoserver.game.model.Season;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.session.SessionFanout;
import com.github.mayconr.juoserver.game.session.SessionOutbound;
import com.github.mayconr.juoserver.game.world.WorldService;
import com.github.mayconr.juoserver.network.packet.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;

@Slf4j
@RequiredArgsConstructor
class InitializationService {

    private final UOMobile mobile;
    private final EventBus eventBus;
    private final WorldService worldService;
    private final SessionOutbound outbound;
    private final SessionFanout fanout;

    public void initialize(PlayerSession session, String clientVersion) {
        worldService.getMobilesInRange(mobile)
            .thenCombine(worldService.getItemsInRange(mobile), (mobiles, items)->{
                finalizeLogin(mobiles, items, session);
                return null;
            }).exceptionally(throwable -> {
                log.info("Erro to init session", throwable);
                return null;
            });
    }

    private void finalizeLogin(Collection<UOMobile> mobiles, Collection<UOItem> items, PlayerSession session) {
        outbound.write(new LoginConfirm(mobile, 7168, 4096));
        outbound.write(new SeasonalInformation(Season.Summer, true));

        for (UOMobile someone : mobiles) {
            if (!someone.equals(mobile)) {
                outbound.write(new DrawMobile(someone));
            }
        }

        for (UOItem item : items) {
            outbound.write(new ObjectInfo(item));
        }

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
