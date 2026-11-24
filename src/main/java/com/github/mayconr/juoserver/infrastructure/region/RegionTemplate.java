package com.github.mayconr.juoserver.infrastructure.region;

import com.github.mayconr.juoserver.infrastructure.template.BaseTemplate;

import java.util.Map;

public record RegionTemplate(
        String name,
        String displayName,
        String parentName,
        String type,
        AreaTemplate area,
        Map<String, Object> properties,
        boolean aiActivationScope,
        RegionLore lore,
        RegionTeleport teleport
) implements BaseTemplate {
    public RegionTemplate {
        if (properties == null) {
            properties = Map.of();
        }
    }
}
