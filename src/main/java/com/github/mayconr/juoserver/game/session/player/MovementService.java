package com.github.mayconr.juoserver.game.session.player;

import com.github.mayconr.juoserver.common.event.EventBus;
import com.github.mayconr.juoserver.common.event.MobileMoved;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.session.SessionFanout;
import com.github.mayconr.juoserver.game.session.SessionOutbound;
import com.github.mayconr.juoserver.game.session.world.WorldSession;
import com.github.mayconr.juoserver.network.packet.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;

@Slf4j
@RequiredArgsConstructor
class MovementService {

    private record WorldView(Collection<UOMobile> mobiles, Collection<UOItem> items) { }
    private final UOPlayer player;
    private final EventBus eventBus;
    private final SessionOutbound outbound;
    private final SessionFanout fanout;
    private final WorldSession worldSession;

    public void handleMove(MoveRequest request) {
        if (request == null) {
            return;
        }

        var result = worldSession.tryMove(player, request);

        if (!result.success()) {
            // TODO Refuse movement
            return;
        }

        worldSession.applyMove(player, result);
        outbound.write(new MovementAck(request.getSequence(), player.getNotoriety()));
        drawWorld(result);
    }

    public void handleMove(Location location) {
        final var result = worldSession.tryMove(player, location);
        if (!result.success()) {
            // TODO Refuse movement
            return;
        }

        worldSession.applyMove(player, result);
        outbound.write(new DrawGamePlayer(player));

        drawWorld(result);
    }

    private void drawWorld(MovementResult result) {

        var mobilesFuture = worldSession.getMobilesInRange(player);
        var itemsFuture   = worldSession.getItemsInRange(player);

        mobilesFuture
            .thenCombine(itemsFuture, WorldView::new)
            .thenAccept(view -> handleView(view.mobiles(), view.items(), result))
            .exceptionally(throwable -> {
                log.error("Failed to load entities in range", throwable);
                return null;
            });
    }

    private void handleView(Collection<UOMobile> mobiles, Collection<UOItem> items, MovementResult result) {
        mobiles.stream()
                .filter(someone -> !someone.equals(player))
                .forEach(someone -> outbound.write(new DrawMobile(someone)));

        items.forEach(item->outbound.write(new ObjectInfo(item)));
        outbound.flush();

        // Broadcast close players
        // TODO drawMobile when enter on range, after that update player
        fanout.write(new UpdatePlayer(player));
        fanout.flush();

        // Notify event
        eventBus.publish(new MobileMoved(player, result));
    }



}
