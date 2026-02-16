package com.github.mayconr.juoserver.infrastructure.region;

import com.github.mayconr.juoserver.game.model.RegionType;
import com.github.mayconr.juoserver.infrastructure.template.TemplateLoader;
import com.github.mayconr.juoserver.game.template.definitions.region.AreaTemplate;
import com.github.mayconr.juoserver.game.template.definitions.region.RegionTemplate;

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

            if (template.properties() != null) {
                template.properties().forEach(node::addProperty);
            }

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

    private RegionArea createArea(AreaTemplate area) {
        if ("rectangle".equalsIgnoreCase(area.type())) {
            return new RectangularArea(
                    area.x(),
                    area.y(),
                    area.width(),
                    area.height()
            );
        }

        throw new IllegalArgumentException("Unsupported area type: " + area.type());
    }
}
