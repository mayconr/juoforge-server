package com.github.mayconr.shard.skills.crafting.mining;

import com.github.mayconr.shard.skills.crafting.CraftingResource;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum OreType implements CraftingResource {
    IRON_ORE(0),
    DULL_COPPER_ORE(30),
    SHADOW_ORE(50),
    COPPER_ORE(70),
    BRONZE_ORE(85),
    GOLD_ORE(100);

    private final double minSkill;

}
