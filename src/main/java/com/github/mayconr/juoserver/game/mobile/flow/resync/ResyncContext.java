package com.github.mayconr.juoserver.game.mobile.flow.resync;

import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractSyncFlowContext;
import com.github.mayconr.juoserver.network.packet.MoveResyncAck;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ResyncContext extends AbstractSyncFlowContext<Void> {
    private final UOPlayer player;
    private final MoveResyncAck resyncAck;

    private ResyncContext(UOPlayer player, MoveResyncAck resyncAck) {
        this.player = player;
        this.resyncAck = resyncAck;
    }

    public static ResyncContext of(UOPlayer player, MoveResyncAck resyncAck) {
        return new ResyncContext(player, resyncAck);
    }
}
