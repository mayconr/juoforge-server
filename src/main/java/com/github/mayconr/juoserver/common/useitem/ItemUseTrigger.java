package com.github.mayconr.juoserver.common.useitem;

public interface ItemUseTrigger {
    boolean supports(ItemUseContext ctx);

    void execute(ItemUseContext ctx);
}
