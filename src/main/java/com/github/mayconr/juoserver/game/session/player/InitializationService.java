package com.github.mayconr.juoserver.game.session.player;

import com.github.mayconr.juoserver.game.event.EventBus;
import com.github.mayconr.juoserver.game.model.Season;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.event.PlayerSessionStarted;
import com.github.mayconr.juoserver.game.session.SessionFanout;
import com.github.mayconr.juoserver.game.session.SessionOutbound;
import com.github.mayconr.juoserver.game.session.world.WorldInternal;
import com.github.mayconr.juoserver.network.packet.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
class InitializationService {

    private final UOMobile mobile;
    private final EventBus eventBus;
    private final WorldInternal worldInternal;
    private final SessionOutbound outbound;
    private final SessionFanout fanout;

    public void initialize(PlayerSession session, String clientVersion) {
        final var mobiles = worldInternal.getMobilesInRange(mobile, 24);
        final var items = worldInternal.getItemsInRange(mobile, 24);
        finalizeLogin(mobiles, items, session);
    }

    private void finalizeLogin(List<UOMobile> mobiles, List<UOItem> items, PlayerSession session) {
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

        outbound.write(new SendSkill(mobile));
        outbound.write(new DrawGamePlayer(mobile));
        outbound.write(new DrawMobile(mobile));
        outbound.write(new StatusBarInfo(mobile));
        outbound.write(new LoginComplete());
        outbound.flush();

        log.info("Session initialized for {}", mobile);

        eventBus.publish(new PlayerSessionStarted(session));
    }

}
