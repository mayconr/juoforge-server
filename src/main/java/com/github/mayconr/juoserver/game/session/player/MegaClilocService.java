package com.github.mayconr.juoserver.game.session.player;

import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.session.SessionOutbound;
import com.github.mayconr.juoserver.network.packet.MegaCliloc;
import com.github.mayconr.juoserver.game.world.WorldService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
class MegaClilocService {

    private final UOMobile mobile;
    private final SessionOutbound outbound;
    private final WorldService worldService;

    public void handleMegaCliloc(List<Integer> serialList) {
        for (int serialId : serialList) {
            if (worldService.isMobile(serialId)) {
                worldService
                    .findMobileBySerialId(serialId)
                    .whenComplete((opt, throwable) -> {
                        if (throwable != null) {
                            log.error("Unable to load mobile [{}]", serialId, throwable);
                        }
                    })
                    .thenAccept(opt -> {
                        opt.map(MegaCliloc::new).ifPresent(outbound::writeAndFlush);
                    });
            } else {
                worldService.findItemBySerialId(serialId)
                    .whenComplete((opt, throwable) -> {
                        if (throwable != null) {
                            log.error("Unable to load item [{}]", serialId, throwable);
                        }
                    })
                    .thenAccept(opt ->
                            opt.map(MegaCliloc::new)
                                    .ifPresent(outbound::writeAndFlush)
                    );
            }
        }
    }
}
