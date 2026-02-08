package com.github.mayconr.juoserver.game.trigger.item;

public interface ItemUseTrigger {
    boolean supports(ItemUseContext ctx);

    void execute(ItemUseContext ctx);
}
