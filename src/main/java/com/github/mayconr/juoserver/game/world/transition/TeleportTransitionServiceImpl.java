package com.github.mayconr.juoserver.game.world.transition;

import com.github.mayconr.juoserver.game.mobile.MobileModule;
import com.github.mayconr.juoserver.game.model.PointInTheWorld;
import com.github.mayconr.juoserver.game.model.RegionType;
import com.github.mayconr.juoserver.game.model.event.MobileRegionChanged;
import lombok.RequiredArgsConstructor;

import java.util.function.Predicate;

@RequiredArgsConstructor
public class TeleportTransitionServiceImpl implements TeleportTransitionService {

    public static final String TELEPORT_DESTINATION_X = "teleport.destination.x";
    public static final String TELEPORT_DESTINATION_Y = "teleport.destination.y";
    public static final String TELEPORT_DESTINATION_Z = "teleport.destination.z";
    private final MobileModule mobileModule;

    @Override
    public void handle(MobileRegionChanged event) {
        var properties = event.newRegion().getProperties();
        int x = (int) properties.get(TELEPORT_DESTINATION_X);
        int y = (int) properties.get(TELEPORT_DESTINATION_Y);
        int z = (int) properties.get(TELEPORT_DESTINATION_Z);

        mobileModule.teleport(event.mobile(), new PointInTheWorld(x, y, z));
    }

    @Override
    public Class<MobileRegionChanged> getType() {
        return MobileRegionChanged.class;
    }

    @Override
    public Predicate<MobileRegionChanged> getPredicate() {
        return event-> RegionType.TELEPORT.equals(event.newRegion().getType());
    }
}
