package com.github.mayconr.juoserver.infrastructure.region;

import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.infrastructure.template.TemplateLoader;
import lombok.RequiredArgsConstructor;

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class MapRegionSystemImpl implements MapRegionSystem {
    private final Map<String, RegionNode> regions;

    public MapRegionSystemImpl(TemplateLoader<RegionTemplate> templateLoader) {
        this.regions = new RegionTemplateMapper().convert(templateLoader);
    }

    @Override
    public void registerRegion(RegionNode region) {
        regions.put(region.getName(), region);
    }

    @Override
    public Optional<RegionNode> getRegion(String name) {
        return Optional.ofNullable(regions.get(name));
    }

    @Override
    public Optional<RegionNode> resolveRegion(Location location) {
        return regions.values().stream()
                .filter(region -> region.contains(location))
                .max(Comparator.comparingInt(this::depth));
    }

    private int depth(RegionNode node) {
        int depth = 0;
        RegionNode current = node;

        while (current.getParent().isPresent()) {
            depth++;
            current = current.getParent().get();
        }

        return depth;
    }
}
