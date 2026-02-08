package com.github.mayconr.shard.skills.crafting;

public interface ResourceRoller {
    <T extends CraftingResource> T rollResource(T[] resources, double miningSkill);
}
