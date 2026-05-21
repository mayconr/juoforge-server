package com.github.mayconr.juoserver.game.mobile.flow.teleport;

import com.github.mayconr.juoserver.game.model.Direction;
import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractSyncFlowContext;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class TeleportContext extends AbstractSyncFlowContext<Void> {
    private final UOMobile mobile;
    private final Location location;

    private Direction direction;

    private TeleportContext(UOMobile mobile, Location location) {
        this.mobile = mobile;
        this.location = location;
    }

    public static TeleportContext of(UOMobile mobile, Location location) {
        return new TeleportContext(mobile, location);
    }
}
