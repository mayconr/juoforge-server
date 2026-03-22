package com.github.mayconr.shard.skills.crafting.mining;

import com.github.mayconr.shard.skills.crafting.CraftingResource;

public record Ore(String name, String itemName, double minSkill) implements CraftingResource {

}
