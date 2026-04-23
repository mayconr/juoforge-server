package com.github.mayconr.juoserver.game.flow;

import com.github.mayconr.juoserver.game.mobile.flow.unequip.LoadUnequipItemStep;
import com.github.mayconr.juoserver.game.mobile.flow.unequip.NotifyUnequipItemStep;
import com.github.mayconr.juoserver.game.mobile.flow.unequip.UnequipItemStep;
import com.github.mayconr.juoserver.game.mobile.flow.unequip.ValidateUnequipItemStep;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.SyncFlowContext;
import com.github.mayconr.juoserver.infrastructure.flow.FlowFactory;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import com.github.mayconr.juoserver.network.packet.UnequipItem;
import lombok.Data;
import lombok.EqualsAndHashCode;

public final class UnequipItemFlowDefinition {
    private UnequipItemFlowDefinition() {
    }

    public static Flow<UnequipItemContext> build(RealmStorage storage, EventBus eventBus) {
        return FlowFactory.<UnequipItemContext>builder()
                .step(new LoadUnequipItemStep(100, storage), context->context.item == null)
                .step(new ValidateUnequipItemStep(200))
                .step(new UnequipItemStep(300))
                .step(new NotifyUnequipItemStep(400, eventBus))
                .build();
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class UnequipItemContext extends SyncFlowContext {
        private final UOMobile mobile;

        private UnequipItem unequipItem;
        private UOItem item;

        private boolean unequipped;

        public UnequipItemContext(UOMobile mobile, UOItem item) {
            this.mobile = mobile;
            this.item = item;
        }

        public UnequipItemContext(UOPlayer mobile, UnequipItem unequipItem) {
            this.mobile = mobile;
            this.unequipItem = unequipItem;
        }

    }
}
