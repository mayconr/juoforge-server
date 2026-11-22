package com.github.mayconr.juoserver.game.mobile.flow.teleport;

import com.github.mayconr.juoserver.game.model.Direction;
import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.region.shared.UpdateMobileRegionContext;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractSyncFlowContext;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class TeleportContext extends AbstractSyncFlowContext<Void> implements UpdateMobileRegionContext {
    private final UOMobile mobile;
    private final Location location;
    private final boolean ignoreObstacles;

    private Direction direction;
    private Location from;

    private TeleportContext(UOMobile mobile, Location location, boolean ignoreObstacles) {
        this.mobile = mobile;
        this.location = location;
        this.ignoreObstacles = ignoreObstacles;
    }

    public static TeleportContext of(UOMobile mobile, Location location) {
        return of(mobile, location, false);
    }

    public static TeleportContext of(UOMobile mobile, Location location, boolean ignoreObstacles) {
        return new TeleportContext(mobile, location, ignoreObstacles);
    }

    public boolean shouldValidateObstacles() {
        return !ignoreObstacles;
    }

    @Override
    public UOMobile mobile() {
        return mobile;
    }

    @Override
    public Location regionFrom() { return from; }

    @Override
    public Location regionTo() { return location; }
}
