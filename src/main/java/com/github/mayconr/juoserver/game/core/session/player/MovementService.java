package com.github.mayconr.juoserver.game.core.session.player;

import com.github.mayconr.juoserver.game.core.event.EventBus;
import com.github.mayconr.juoserver.game.core.event.MobileMove;
import com.github.mayconr.juoserver.game.core.model.*;
import com.github.mayconr.juoserver.game.packet.*;
import com.github.mayconr.juoserver.game.storage.MobileFilter;
import com.github.mayconr.juoserver.game.storage.WorldService;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.group.ChannelGroup;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
class MovementService {

    private final UOPlayer player;
    private final EventBus eventBus;
    private final ChannelGroup channelGroup;
    private final ChannelHandlerContext ctx;
    private final WorldService worldService;

    public void handleMove(MoveRequest moveRequest) {
        final var direction = moveRequest.getDirection();

        if (player.getDirection().equals(direction)) {
            player.move(direction);
            worldService.moveMobile(player);
        }

        player.setRunning(moveRequest.isRunning());
        player.setDirection(direction);

        ctx.write(new MovementAck(
                moveRequest.getSequence(),
                player.getNotoriety()
        ));

        var mobilesFuture = worldService.getMobilesInRange(player);
        var itemsFuture   = worldService.getItemsInRange(player);

        mobilesFuture
                .thenCombine(itemsFuture, (mobiles, items) -> {
                    mobilesInRangeLoaded(mobiles);
                    itemsInRangeLoaded(items);
                    return null;
                })
                .exceptionally(ex -> {
                    log.error("Failed to load entities in range", ex);
                    return null;
                });
        ctx.flush();

        eventBus.publish(new MobileMove(player, direction));
    }

    public void handleMove(Location location) {
        player.setLocation(location.getX(), location.getY(), location.getZ());

        ctx.write(new DrawGamePlayer(player));

        var mobilesFuture = worldService.getMobilesInRange(player);
        var itemsFuture   = worldService.getItemsInRange(player);

        mobilesFuture
                .thenCombine(itemsFuture, (mobiles, items) -> {
                    mobilesInRangeLoaded(mobiles);
                    itemsInRangeLoaded(items);
                    return null;
                })
                .exceptionally(ex -> {
                    log.error("Failed to load entities in range after move", ex);
                    return null;
                });

        ctx.flush();

        channelGroup.writeAndFlush(
                new UpdatePlayer(player)
        ); // TODO filter by range
    }

    private void mobilesInRangeLoaded(List<UOMobile> mobiles) {
        mobiles.stream()
                .filter(someone -> !someone.equals(player))
                .forEach(someone -> ctx.write(new DrawMobile(someone)));
        ctx.flush();
    }

    private void itemsInRangeLoaded(List<UOItem> items) {
        items.forEach(item->ctx.write(new ObjectInfo(item)));
        ctx.flush();

        channelGroup.writeAndFlush(
                new UpdatePlayer(player),
                channel -> !channel.equals(ctx.channel())); // TODO only for close mobiles
    }
}
