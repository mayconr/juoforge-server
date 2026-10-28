package com.github.mayconr.juoserver.game.player.flow.spawn;

import com.github.mayconr.juoserver.game.region.shared.UpdateMobileRegionContext;
import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractSyncFlowContext;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@Getter
@RequiredArgsConstructor
public final class PlayerSpawnContext extends AbstractSyncFlowContext<Void> implements UpdateMobileRegionContext {
    private final UOPlayer player;
    private final Map<Integer, UOPlayer> onlinePlayers;

    @Override
    public UOMobile mobile() {
        return player;
    }

    @Override
    public Location regionFrom() { return null; }

    @Override
    public Location regionTo() { return player; }
}
