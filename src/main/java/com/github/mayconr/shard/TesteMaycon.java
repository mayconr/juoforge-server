package com.github.mayconr.shard;

import com.github.mayconr.juoserver.game.model.MessageOptions;
import com.github.mayconr.juoserver.game.model.event.MobileGoldChanged;
import com.github.mayconr.juoserver.game.model.event.VendorPurchaseCompleted;
import com.github.mayconr.juoserver.game.world.World;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.function.Predicate;

@Component
@RequiredArgsConstructor
public class TesteMaycon implements EventRegistry<MobileGoldChanged> {

    private final World world;


    @Override
    public Class<MobileGoldChanged> getType() {
        return MobileGoldChanged.class;
    }

    @Override
    public Predicate<MobileGoldChanged> getPredicate() {
        return e->true;
    }

    @Override
    public void handle(MobileGoldChanged event) {
        System.out.println(event.newBalance());
    }
}
