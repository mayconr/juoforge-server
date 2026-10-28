package com.github.mayconr.juoserver.game.mobile.flow.movement;

import com.github.mayconr.juoserver.game.model.Direction;
import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.region.shared.UpdateMobileRegionContext;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractSyncFlowContext;
import com.github.mayconr.juoserver.network.packet.MoveRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@RequiredArgsConstructor
@Data
public class MovementContext extends AbstractSyncFlowContext<Void> implements UpdateMobileRegionContext {

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
    private Location from;
    private MoveIntent moveIntent;
    private int sequence;

    @Override
    public UOMobile mobile() { return mobile; }

    @Override
    public Location regionFrom() { return from; }

    @Override
    public Location regionTo() { return targetLocation; }
}
