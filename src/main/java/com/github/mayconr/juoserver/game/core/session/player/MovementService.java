package com.github.mayconr.juoserver.game.core.session.player;

import com.github.mayconr.juoserver.game.core.database.Database;
import com.github.mayconr.juoserver.game.core.database.MobileFilter;
import com.github.mayconr.juoserver.game.core.event.EventBus;
import com.github.mayconr.juoserver.game.core.event.MobileMove;
import com.github.mayconr.juoserver.game.core.model.Location;
import com.github.mayconr.juoserver.game.core.model.UOPlayer;
import com.github.mayconr.juoserver.game.packet.*;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.group.ChannelGroup;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class MovementService {

    private final UOPlayer player;
    private final EventBus eventBus;
    private final ChannelGroup channelGroup;
    private final ChannelHandlerContext ctx;
    private final Database database;

    public void handleMove(MoveRequest moveRequest) {
        final var direction = moveRequest.getDirection();

        if (player.getDirection().equals(direction)) {
            player.move(direction);
        }
        player.setRunning(moveRequest.isRunning());
        player.setDirection(direction);

        ctx.write(new MovementAck(moveRequest.getSequence(), player.getNotoriety()));
        database.getMobilesInRange(player, MobileFilter.ALL_VISIBLE)
                .filter(someone -> !someone.equals(player)) // avoid unnecessary packet
                .forEach(someone -> ctx.write(new DrawMobile(someone)));
        database.getItemsInRange(player).forEach(item -> ctx.write(new ObjectInfo(item)));
        ctx.flush();
        channelGroup.writeAndFlush(
                new UpdatePlayer(player),
                channel -> !channel.equals(ctx.channel())); // TODO only for close mobiles

        eventBus.publish(new MobileMove(player, direction));
    }

    public void handleMove(Location location) {
        player.setLocation(location.getX(), location.getY(), location.getZ());
        ctx.write(new DrawGamePlayer(player));
        database.getMobilesInRange(player, MobileFilter.ALL_VISIBLE)
                .filter(someone -> !someone.equals(player)) // avoid unnecessary packet
                .forEach(someone -> ctx.write(new DrawMobile(someone)));
        ctx.flush();
        channelGroup.writeAndFlush(new UpdatePlayer(player)); // TODO only for close mobiles
    }
}
