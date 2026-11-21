package com.github.mayconr.juoserver.game.mobile.flow.unequip;

import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.ItemTarget;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractSyncFlowContext;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class UnequipItemContext extends AbstractSyncFlowContext<Void> {
    private final UOMobile mobile;

    private UOItem item;
    private final ItemTarget target;

    private boolean unequipped;

    public UnequipItemContext(UOMobile mobile, UOItem item) {
        this(mobile, item, ItemTarget.orphan());
    }

    public UnequipItemContext(UOMobile mobile, UOItem item, ItemTarget target) {
        this.mobile = mobile;
        this.item = item;
        this.target = target;
    }

}
