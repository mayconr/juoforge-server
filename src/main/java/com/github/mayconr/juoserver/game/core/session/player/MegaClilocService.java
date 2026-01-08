package com.github.mayconr.juoserver.game.core.session.player;

import java.util.List;

import com.github.mayconr.juoserver.game.core.database.Database;
import com.github.mayconr.juoserver.game.core.model.UOMobile;
import com.github.mayconr.juoserver.game.packet.MegaCliloc;

import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class MegaClilocService {

    private final UOMobile mobile;
    private final ChannelHandlerContext ctx;
    private final Database database;

    public void handleMegaCliloc(List<Integer> serialList) {
        for (int serialId : serialList) {
            if (database.isMobile(serialId)) {
                database.getMobileSerialId(serialId).map(MegaCliloc::new).ifPresent(ctx::write);
            } else {
                database.getItemBySerialId(serialId).map(MegaCliloc::new).ifPresent(ctx::write);
            }
        }
        ctx.flush();
    }
}
