package com.github.mayconr.shard;

import com.github.mayconr.juoserver.game.item.trigger.ItemUseRegistry;
import com.github.mayconr.juoserver.game.item.trigger.ItemUseTrigger;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class GlobalRegister implements SmartInitializingSingleton {

    private final ApplicationContext context;
    private final ItemUseRegistry itemUseRegistry;
    private final EventBus eventBus;

    @Override
    public void afterSingletonsInstantiated() {
        for (var trigger : context.getBeansOfType(ItemUseTrigger.class).values()) {
            itemUseRegistry.register(trigger);
        }
        log.info("Items use registered");

        for (EventRegistry registry : context.getBeansOfType(EventRegistry.class).values()) {
           eventBus.register(registry);
        }
    }
}
