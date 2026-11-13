package com.github.mayconr.juoserver.game.item.flow.equip;

import com.github.mayconr.juoserver.game.item.hold.HeldItemRequest;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractSyncFlowContext;
import com.github.mayconr.juoserver.network.packet.EquipItemRequest;
import lombok.Getter;
import lombok.Setter;

/** Client request to equip the item on the player's cursor. */
@Getter
@Setter
public class EquipHeldItemContext extends AbstractSyncFlowContext<Void> implements HeldItemRequest {
    private final UOPlayer player;
    private final EquipItemRequest request;

    private UOItem item;

    public EquipHeldItemContext(UOPlayer player, EquipItemRequest request) {
        this.player = player;
        this.request = request;
    }

    @Override
    public int requestedItemSerial() {
        return request.getItemSerialId();
    }
}
