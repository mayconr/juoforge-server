package com.github.mayconr.juoserver.infrastructure.region;

import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.RegionType;
import com.github.mayconr.juoserver.infrastructure.template.TemplateLoader;
import lombok.RequiredArgsConstructor;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class RegionSystemImpl implements RegionSystem {
    private final Map<String, RegionNode> regions;

    public RegionSystemImpl(TemplateLoader<RegionTemplate> templateLoader) {
        this.regions = new RegionTemplateMapper().convert(templateLoader);
    }

    @Override
    public Optional<RegionNode> getRegion(String name) {
        return Optional.ofNullable(regions.get(name));
    }

    @Override
    public Optional<RegionNode> getRegion(Location location) {
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

    @Override
    public Optional<RegionNode> findAiActivationRegion(RegionNode region) {
        RegionNode current = region;
        while (current != null) {
            if (current.isAiActivationScope()) {
                return Optional.of(current);
            }
            current = current.getParent().orElse(null);
        }
        return Optional.empty();
    }

    @Override
    public List<RegionNode> getRegionsByType(RegionType type) {
        return regions.values().stream()
                .filter(region->region.getType().equals(type))
                .toList();
    }
}
