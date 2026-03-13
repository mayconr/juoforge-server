package com.github.mayconr.shard.skills.crafting.lumberjacking;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LogableStatic {

    NORMAL_TREE(3273, 3306),
    COMPOSE_TREE(3393, 3499);

    private final int min;
    private final int max;

}
