package com.github.mayconr.juoserver.game.mobile.flow.equip;

import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.infrastructure.flow.SyncFlowContext;
import com.github.mayconr.juoserver.network.packet.EquipItemRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class EquipItemContext extends SyncFlowContext<Void> {
    private final UOMobile mobile;

    private EquipItemRequest equipItem;
    private UOItem item;

    private boolean equipped;

    public EquipItemContext(UOMobile mobile, EquipItemRequest equipItem) {
        this.mobile = mobile;
        this.equipItem = equipItem;
    }

    public EquipItemContext(UOMobile mobile, UOItem item) {
        this.mobile = mobile;
        this.item = item;
    }
}
