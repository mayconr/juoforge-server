package com.github.mayconr.juoserver.game.flow;

import com.github.mayconr.juoserver.game.mobile.flow.equip.*;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.SyncFlowContext;
import com.github.mayconr.juoserver.infrastructure.flow.FlowFactory;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import com.github.mayconr.juoserver.network.packet.EquipItemRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

public class EquipItemFlowDefinition {
    private EquipItemFlowDefinition() {
    }

    public static Flow<EquipItemContext> build(RealmStorage storage, EventBus  eventBus) {
        return FlowFactory.<EquipItemContext>builder()
                .step(new LoadEquipItemStep(100, storage), context->context.item == null)
                .step(new ValidateItemEquipStep(100))
                .step(new CleanupItemStateStep(200, storage))
                .step(new EquipItemStep(300))
                .step(new NotifyItemEquipped(400, eventBus))
                .build();
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class EquipItemContext extends SyncFlowContext {
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
}
