package com.github.mayconr.shard;

import com.github.mayconr.juoserver.game.item.trigger.ItemUseRegistry;
import com.github.mayconr.juoserver.game.item.trigger.ItemUseTrigger;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventRegistry;
import com.github.mayconr.shard.storage.ItemMapper;
import com.github.mayconr.shard.storage.MobileMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class GlobalRegister implements SmartInitializingSingleton {

    private final ApplicationContext context;
    private final ItemUseRegistry itemUseRegistry;
    private final EventBus eventBus;
    private final SqlSessionFactory sessionFactory;

    @Override
    public void afterSingletonsInstantiated() {
        for (var trigger : context.getBeansOfType(ItemUseTrigger.class).values()) {
            itemUseRegistry.register(trigger);
        }
        log.info("Items use registered");

        for (EventRegistry registry : context.getBeansOfType(EventRegistry.class).values()) {
           eventBus.register(registry);
        }

        try (var session = sessionFactory.openSession()) {
            var mapper = session.getMapper(ItemMapper.class);
            //System.out.println(mapper.findAllEquippedItems(UUID.fromString("3c846a31-420e-4665-a094-d872cba46dc3")));
            System.out.println(mapper.findAllGroundItems());
            //var mobile = mapper.findMobileById(UUID.fromString("b8999205-34ec-4fb5-bde5-e15b44f4d425"));
            //System.out.println(mobile);
        }

    }
}
