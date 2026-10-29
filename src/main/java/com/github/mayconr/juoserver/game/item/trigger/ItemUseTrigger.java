package com.github.mayconr.juoserver.game.item.trigger;

@FunctionalInterface
public interface ItemUseTrigger {
    void execute(ItemUseContext ctx);
}
