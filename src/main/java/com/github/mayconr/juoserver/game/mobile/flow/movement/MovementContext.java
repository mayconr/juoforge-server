package com.github.mayconr.juoserver.game.mobile.flow.movement;

import com.github.mayconr.juoserver.game.model.Direction;
import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractSyncFlowContext;
import com.github.mayconr.juoserver.network.packet.MoveRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@RequiredArgsConstructor
@Data
public class MovementContext extends AbstractSyncFlowContext<Void> {

    public enum MoveIntent {
        TURN, FORWARD;
    }

    private final UOMobile mobile;
    private final Direction direction;
    private final boolean running;
    private final MoveRequest moveRequest;
    private final boolean isRequested;

    public static MovementContext of(UOMobile mobile, MoveRequest moveRequest) {
        return new MovementContext(mobile, moveRequest.getDirection(), moveRequest.isRunning(), moveRequest, true);
    }

    public static MovementContext of(UOMobile mobile, Direction direction, boolean running) {
        return new MovementContext(mobile, direction, running, null, false);
    }

    private Location targetLocation;
    private MoveIntent moveIntent;
    private int sequence;
}
