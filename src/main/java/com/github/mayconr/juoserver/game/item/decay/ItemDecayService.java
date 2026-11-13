package com.github.mayconr.juoserver.game.item.decay;

import com.github.mayconr.juoserver.game.GamePlaySettings.ItemDecay;
import com.github.mayconr.juoserver.game.item.flow.decay.DecayItemContext;
import com.github.mayconr.juoserver.game.world.context.ModuleContext.FlowFacade;
import com.github.mayconr.juoserver.infrastructure.storage.item.Items;
import lombok.extern.slf4j.Slf4j;

/** Periodically discovers ground candidates across the entire world, without relying on events. */
@Slf4j
public final class ItemDecayService {
    private final Items items;
    private final ItemDecayClock clock;
    private final ItemDecay settings;
    private final FlowFacade flows;
    private double elapsed;

    public ItemDecayService(Items items, ItemDecayClock clock, ItemDecay settings, FlowFacade flows) {
        this.items = items;
        this.clock = clock;
        this.settings = settings;
        this.flows = flows;
    }

    public void update(double delta) {
        clock.advance(delta);
        elapsed += delta;
        if (elapsed + 1e-9 < settings.scanIntervalSeconds()) {
            return;
        }
        elapsed %= settings.scanIntervalSeconds();
        if (elapsed + 1e-9 >= settings.scanIntervalSeconds()) {
            elapsed = 0;
        }
        // Snapshot before spilling containers: their children participate in a later scan.
        for (var item : items.onGround()) {
            var result = flows.execute(new DecayItemContext(item));
            if (result.flowFailed()) {
                log.error("Item decay failed: serial={}, code={}, reason={}",
                        item.getSerialId(), result.code(), result.reason(), result.cause());
            }
        }
    }
}
