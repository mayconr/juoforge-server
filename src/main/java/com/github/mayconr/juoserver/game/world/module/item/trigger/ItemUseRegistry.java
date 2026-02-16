package com.github.mayconr.juoserver.game.world.module.item.trigger;

import java.util.ArrayList;
import java.util.List;

public class ItemUseRegistry {
    private final List<ItemUseTrigger> triggers = new ArrayList<>();

    /**
     * Registers a new item use trigger.
     */
    public void register(ItemUseTrigger trigger) {
        triggers.add(trigger);
    }

    /**
     * Finds and executes the first trigger that supports the given context.
     */
    public boolean dispatch(ItemUseContext context) {
        for (ItemUseTrigger trigger : triggers) {
            if (trigger.supports(context)) {
                trigger.execute(context);
                return true;
            }
        }
        return false;
    }
}
