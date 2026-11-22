package com.github.mayconr.juoserver.game.ui.flow.doubleclick;

import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractSyncFlowContext;
import com.github.mayconr.juoserver.network.packet.DoubleClick;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public final class DoubleClickContext extends AbstractSyncFlowContext<Void> {
    private final UOPlayer player;
    private final DoubleClick request;

    public boolean targetsMobile() {
        return request.isPaperdollRequest() || UOMobile.isMobile(request.getSerialId());
    }

    public int paperdollSerial() {
        return request.isPaperdollRequest() ? player.getSerialId() : request.getSerialId();
    }
}
