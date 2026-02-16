package com.github.mayconr.juoserver.game.world.module.item.trigger;

public interface ItemUseTrigger {
    boolean supports(ItemUseContext ctx);

    void execute(ItemUseContext ctx);
}
