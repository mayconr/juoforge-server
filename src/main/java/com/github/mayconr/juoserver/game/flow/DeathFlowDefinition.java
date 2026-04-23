package com.github.mayconr.juoserver.game.flow;

import com.github.mayconr.juoserver.game.item.ItemModule;
import com.github.mayconr.juoserver.game.item.flow.death.CorpseCreationStep;
import com.github.mayconr.juoserver.game.mobile.MobileModule;
import com.github.mayconr.juoserver.game.mobile.flow.death.*;
import com.github.mayconr.juoserver.game.model.DeathCause;
import com.github.mayconr.juoserver.game.model.UOCorpse;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UOObject;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.FlowFactory;
import com.github.mayconr.juoserver.infrastructure.flow.SyncFlowContext;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

public final class DeathFlowDefinition {
    private DeathFlowDefinition() {
    }

    public static Flow<DeathContext> build(ItemModule itemModule, MobileModule mobileModule, EventBus eventBus, RealmStorage storage) {
        return FlowFactory.<DeathContext>builder()
                .step(new ValidateDeathStep())
                .step(new CorpseCreationStep(itemModule))
                .step(new MoveItemsToCorpseStep(mobileModule, itemModule, storage))
                .step(new UpdateMobileDeathStatusStep())
                .step(new SpawnMobileMountStep())
                .step(new NotifyDeathStep(eventBus))
                .build();
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @RequiredArgsConstructor
    public static class DeathContext extends SyncFlowContext {

        private final UOMobile victim;
        private final UOObject<?> killer;
        private final DeathCause cause;
        private UOCorpse corpse;

        public void corpse(UOCorpse corpse) {
            this.corpse = corpse;
        }
    }
}
