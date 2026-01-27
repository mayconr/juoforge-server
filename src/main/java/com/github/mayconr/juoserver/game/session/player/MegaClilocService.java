package com.github.mayconr.juoserver.game.session.player;

import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.session.SessionOutbound;
import com.github.mayconr.juoserver.game.session.world.WorldSession;
import com.github.mayconr.juoserver.network.packet.MegaCliloc;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
class MegaClilocService {

    private final UOMobile mobile;
    private final SessionOutbound outbound;
    private final WorldSession worldSession;

    public void handleMegaCliloc(List<Integer> serialList) {
        for (int serialId : serialList) {
            if (worldSession.isMobile(serialId)) {
                worldSession.findMobileBySerialId(serialId)
                    .thenAccept(mobile -> outbound.write(new MegaCliloc(mobile)))
                    .whenComplete((unused, throwable) ->this.logging(throwable, serialId));
            } else {
                worldSession.findItemBySerialId(serialId)
                    .thenAccept(item -> outbound.write(new MegaCliloc(item)))
                    .whenComplete((unused, throwable) -> this.logging(throwable, serialId));
            }
        }
    }

    private void logging(Throwable throwable, int serialId) {
        if (throwable != null) {
            log.error("Unable to load item [{}]", serialId, throwable);
        }
    }
}
