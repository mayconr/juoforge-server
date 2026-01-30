package com.github.mayconr.juoserver.game.session.player;

import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.session.SessionOutbound;
import com.github.mayconr.juoserver.game.session.world.WorldInternal;
import com.github.mayconr.juoserver.network.packet.MegaCliloc;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
class MegaClilocService {

    private final UOMobile mobile;
    private final SessionOutbound outbound;
    private final WorldInternal worldInternal;

    public void handleMegaCliloc(List<Integer> serialList) {
        for (int serialId : serialList) {
            if (UOMobile.isMobile(serialId)) {
                worldInternal.getMobileBySerialId(serialId)
                        .ifPresent(mobile-> outbound.write(new MegaCliloc(mobile)));
            } else {
                worldInternal.getItemBySerialId(serialId)
                        .ifPresent(item-> outbound.write(new MegaCliloc(item)));
            }
        }
    }

}
