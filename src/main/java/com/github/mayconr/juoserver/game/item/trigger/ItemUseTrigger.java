package com.github.mayconr.juoserver.game.item.trigger;

public interface ItemUseTrigger {
    boolean supports(ItemUseContext ctx);

    void execute(ItemUseContext ctx);
}
