package com.github.mayconr.juoserver.common.useitem;

import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOMobile;

public record ItemUseContext(
        UOMobile mobile,
        UOItem item,
        Trigger trigguer
) {}
