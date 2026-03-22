package com.github.mayconr.shard.skills.crafting.mining;

import com.github.mayconr.juoserver.game.item.trigger.ItemUseContext;
import com.github.mayconr.juoserver.game.item.trigger.ItemUseTrigger;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MiningToolTrigger implements ItemUseTrigger {

    private final MiningUseService miningUseService;

    @Override
    public boolean supports(ItemUseContext ctx) {
        return "pickaxe".equals(ctx.item().getName());
    }

    @Override
    public void execute(ItemUseContext ctx) {
        miningUseService.start(ctx);
    }

}
