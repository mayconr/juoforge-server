package com.github.mayconr.juoserver.game.session.player.target;

import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.session.SessionOutbound;
import com.github.mayconr.juoserver.network.packet.Target;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

@Slf4j
@RequiredArgsConstructor
public class TargetService {

    private final UOPlayer player;
    private final SessionOutbound outbound;
    private final Map<Integer, Consumer<TargetResult>> consumerMap = new ConcurrentHashMap<>();
    private final AtomicInteger targetSeq = new AtomicInteger(1);

    public void handleSendTarget(CursorType type, Consumer<TargetResult> consumer) {
        final int targetId = targetSeq.getAndIncrement();
        consumerMap.put(targetId, consumer);

        outbound.writeAndFlush(new Target(targetId, CursorTarget.LOCATION, type));
        log.debug("Target [{}] sent to client", targetId);
    }

    public void handleTarget(Target target) {
        final var consumer = consumerMap.remove(target.getCursorId());
        if (consumer != null) {
            log.debug("Received callback for Target [{}]", target.getCursorId());
            final var result = switch (target.getTarget()) {
                case LOCATION -> new TargetResult(
                        player,
                        TargetType.STATICS,
                        0,
                        target.getModelId(),
                        new PointInTheWorld(target.getX(), target.getY(), target.getZ())
                );
                case OBJECT -> new TargetResult(
                        player,
                        TargetType.OBJECT,
                        target.getClickedSerialId(),
                        0,
                        new PointInTheWorld(target.getX(), target.getY(), target.getZ())
                );
            };
            consumer.accept(result);
        }
    }
}
