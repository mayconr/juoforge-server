package com.github.mayconr.juoserver.game.trigger.item;

import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.trigger.Trigger;

public record ItemUseContext(
        UOPlayer player,
        UOItem item,
        Trigger trigger
) {}
