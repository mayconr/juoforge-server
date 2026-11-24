package com.github.mayconr.juoserver.infrastructure.region;

import com.github.mayconr.juoserver.game.model.RegionType;
import com.github.mayconr.juoserver.infrastructure.template.TemplateLoader;

import java.util.HashMap;
import java.util.Map;

class RegionTemplateMapper {

    public Map<String, RegionNode> convert(TemplateLoader<RegionTemplate> templateLoader) {

        final Map<String, RegionTemplate> templates = templateLoader.load();
        final Map<String, RegionNode> result = new HashMap<>();

        // -------------------------
        // Create nodes
        // -------------------------
        for (RegionTemplate template : templates.values()) {

            final String name = template.name();

            final RegionArea area = createArea(template.area());

            final RegionNode node = new RegionNode(
                    name,
                    template.displayName(),
                    RegionType.valueOf(template.type()),
                    area
            );

            node.setAiActivationScope(template.aiActivationScope());
            node.setLore(template.lore());
            if (node.getType() == RegionType.TELEPORT) {
                node.setTeleport(requireTeleport(template));
            }

            template.properties().forEach(node::addProperty);

            result.put(name, node);
        }

        // -------------------------
        // resolve inheritance
        // -------------------------
        for (RegionTemplate template : templates.values()) {

            if (template.parentName() == null) {
                continue;
            }

            String childId = template.name();
            String parentId = template.parentName();

            RegionNode child = result.get(childId);
            RegionNode parent = result.get(parentId);

            if (parent == null) {
                throw new IllegalStateException("Parent region not found: " + template.parentName());
            }

            parent.addChild(child);
        }

        return result;
    }

    private RegionTeleport requireTeleport(RegionTemplate template) {
        var teleport = template.teleport();
        if (teleport == null || teleport.kind() == null || teleport.kind().isBlank()) {
            throw new IllegalArgumentException("Region '" + template.name() + "' requires teleport.kind");
        }

        var destination = teleport.destination();
        if (destination == null || destination.x() == null || destination.y() == null || destination.z() == null) {
            throw new IllegalArgumentException("Region '" + template.name()
                    + "' requires teleport.destination with x, y and z");
        }
        return teleport;
    }

    private RegionArea createArea(AreaTemplate area) {
        if ("rectangle".equalsIgnoreCase(area.type())) {
            return new RectangularArea(
                    area.x(),
                    area.y(),
                    area.width(),
                    area.height()
            );
        }

        if ("polygon".equalsIgnoreCase(area.type())) {
            if (area.points() == null) {
                throw new IllegalArgumentException("Polygon area requires points.");
            }
            return new PolygonalArea(area.points());
        }

        throw new IllegalArgumentException("Unsupported area type: " + area.type());
    }
}
