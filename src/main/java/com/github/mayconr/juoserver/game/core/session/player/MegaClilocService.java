package com.github.mayconr.juoserver.game.core.session.player;

import com.github.mayconr.juoserver.game.core.model.UOMobile;
import com.github.mayconr.juoserver.game.packet.MegaCliloc;
import com.github.mayconr.juoserver.game.storage.WorldService;
import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
class MegaClilocService {

    private final UOMobile mobile;
    private final ChannelHandlerContext ctx;
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
                        opt.map(MegaCliloc::new).ifPresent(ctx::write);
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
                                    .ifPresent(ctx::write)
                    );
            }
        }
        ctx.flush();
    }
}
