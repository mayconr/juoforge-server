package com.github.mayconr.juoserver.game.world;

import com.github.mayconr.juoserver.game.flow.DamageFlowDefinition;
import com.github.mayconr.juoserver.game.flow.DeathFlowDefinition;
import com.github.mayconr.juoserver.game.flow.DeathFlowDefinition.DeathContext;
import com.github.mayconr.juoserver.game.item.ItemModule;
import com.github.mayconr.juoserver.game.item.ItemRequest;
import com.github.mayconr.juoserver.game.mobile.MobileModule;
import com.github.mayconr.juoserver.game.model.ItemCreationOptions;
import com.github.mayconr.juoserver.game.model.ItemTarget;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.FlowContext;
import com.github.mayconr.juoserver.infrastructure.flow.FlowDescriptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;

@Slf4j
@RequiredArgsConstructor
public class DefaultModuleContext implements ModuleContext {

    private final EventBus eventBus;
    private final ItemModule itemModule;
    private final MobileModule mobileModule;

    @Override
    public ItemFacade items() {
        return new ItemFacade() {
            @Override
            public UOItem create(ItemRequest request, ItemTarget target, Consumer<ItemCreationOptions> options) {
                return itemModule.createItem(request, target, options);
            }

            @Override
            public UOItem create(ItemRequest request, ItemTarget target) {
                return itemModule.createItem(request, target);
            }

        };
    }

    @Override
    public FlowFacade flows() {
        return new FlowFacade() {
            @Override
            public void execute(DeathContext context) {
                executeFlow("Death", DeathFlowDefinition.build(itemModule, mobileModule, eventBus), context);
            }

            @Override
            public void execute(DamageFlowDefinition.DamageContext context) {
                executeFlow("Damage", DamageFlowDefinition.build(mobileModule, eventBus), context);
            }

            private void executeFlow(String name, Flow<? extends FlowContext> flow, FlowContext context) {
                if (log.isDebugEnabled()) {
                    log.debug("Pipeline executed:\n{}", FlowDescriptor.describe(name, flow));
                }
                executeUnchecked(flow, context);
            }

            @SuppressWarnings("unchecked")
            private <T extends FlowContext> void executeUnchecked(Flow<T> flow, FlowContext context) {
                flow.execute((T) context);
            }
        };
    }
}
