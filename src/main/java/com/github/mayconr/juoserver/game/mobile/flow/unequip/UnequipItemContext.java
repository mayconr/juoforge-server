package com.github.mayconr.juoserver.game.mobile.flow.unequip;

import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractSyncFlowContext;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class UnequipItemContext extends AbstractSyncFlowContext<Void> {
    private final UOMobile mobile;

    private UOItem item;

    private boolean unequipped;

    public UnequipItemContext(UOMobile mobile, UOItem item) {
        this.mobile = mobile;
        this.item = item;
    }

}
