package com.github.mayconr.juoserver.game.item.flow.lift;

import com.github.mayconr.juoserver.game.model.LiftRejectReason;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractSyncFlowContext;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.network.packet.UnequipItem;
import lombok.Getter;
import lombok.Setter;

/** Client request to lift an item onto the cursor, and the state gathered while it is validated. */
@Getter
@Setter
public class LiftItemContext extends AbstractSyncFlowContext<Void> {
    private final UOPlayer player;
    private final UnequipItem request;

    private UOItem item;
    private boolean held;
    private LiftRejectReason rejection = LiftRejectReason.INSPECIFIC;

    public LiftItemContext(UOPlayer player, UnequipItem request) {
        this.player = player;
        this.request = request;
    }

    /** Records why the lift is refused and returns the failure that stops the flow. */
    public StepResult reject(LiftRejectReason reason, String detail) {
        this.rejection = reason;
        return StepResult.failure(reason.name(), detail);
    }
}
