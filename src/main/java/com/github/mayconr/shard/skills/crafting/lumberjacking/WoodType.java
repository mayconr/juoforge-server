package com.github.mayconr.shard.skills.crafting.lumberjacking;

import com.github.mayconr.shard.skills.crafting.CraftingResource;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WoodType implements CraftingResource {
    WOOD(0),
    OAK(65),
    ASH(75),
    YEW(85);

    private final double minSkill;
}
