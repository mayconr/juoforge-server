package com.github.mayconr.juoserver.game.world.module.item.trigger;

import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOPlayer;

public record ItemUseContext(
        UOPlayer player,
        UOItem item,
        Trigger trigger
) {}
