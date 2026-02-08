package com.github.mayconr.shard;

import com.github.mayconr.juoserver.game.trigger.item.ItemUseRegistry;
import com.github.mayconr.juoserver.game.trigger.item.ItemUseTrigger;
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

    @Override
    public void afterSingletonsInstantiated() {
        for (var trigger : context.getBeansOfType(ItemUseTrigger.class).values()) {
            itemUseRegistry.register(trigger);
        }
        log.info("Items use registered");
    }
}
