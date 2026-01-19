package com.github.mayconr.juoserver.game.session.player;

import com.github.mayconr.juoserver.common.event.EventBus;
import com.github.mayconr.juoserver.common.event.MobileMoved;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.session.SessionFanout;
import com.github.mayconr.juoserver.game.session.SessionOutbound;
import com.github.mayconr.juoserver.game.world.WorldService;
import com.github.mayconr.juoserver.network.packet.DrawMobile;
import com.github.mayconr.juoserver.network.packet.MoveRequest;
import com.github.mayconr.juoserver.network.packet.MovementAck;
import com.github.mayconr.juoserver.network.packet.ObjectInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;

@Slf4j
@RequiredArgsConstructor
class MovementService {

    private final UOPlayer player;
    private final EventBus eventBus;
    private final SessionOutbound outbound;
    private final SessionFanout fanout;
    private final WorldService worldService;

    public void handleMove(MoveRequest request) {
        if (request == null) {
            return;
        }

        var result = worldService.tryMove(player, request);

        if (!result.success()) {
            // TODO Refuse movement
            return;
        }

        worldService.applyMove(player, result);

        outbound.write(new MovementAck(request.getSequence(), player.getNotoriety()));

        var mobilesFuture = worldService.getMobilesInRange(player);
        var itemsFuture   = worldService.getItemsInRange(player);

        mobilesFuture
            .thenCombine(itemsFuture, WorldView::new)
            .thenAccept(view -> handleView(view.mobiles(), view.items(), result))
            .exceptionally(throwable -> {
                log.error("Failed to load entities in range", throwable);
                return null;
            });
    }

    private record WorldView(Collection<UOMobile> mobiles, Collection<UOItem> items) { }

    private void handleView(Collection<UOMobile> mobiles, Collection<UOItem> items, MovementResult result) {
        mobiles.stream()
                .filter(someone -> !someone.equals(player))
                .forEach(someone -> outbound.write(new DrawMobile(someone)));

        items.forEach(item->outbound.write(new ObjectInfo(item)));
        outbound.flush();

        // Broadcast close players
        fanout.write(new DrawMobile(player));
        //fanout.write(new UpdatePlayer(player));
        fanout.flush();

        // Notify event
        eventBus.publish(new MobileMoved(player, result));
    }

    public void handleMove(Location location) {
        log.info("Not implemented yet");
    }

}
