package com.github.mayconr.shard;

import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.github.mayconr.juoserver.WorldContent;
import com.github.mayconr.juoserver.game.GamePlaySettings;
import com.github.mayconr.juoserver.game.economy.template.RegionStockTemplate;
import com.github.mayconr.juoserver.game.item.template.ItemTemplate;
import com.github.mayconr.juoserver.game.messaging.template.MessageStyleTemplate;
import com.github.mayconr.juoserver.game.mobile.template.MountTemplate;
import com.github.mayconr.juoserver.game.mobile.template.NpcTemplate;
import com.github.mayconr.juoserver.game.mobile.template.NpcStatProfile;
import com.github.mayconr.juoserver.game.mobile.template.NpcAIProfile;
import com.github.mayconr.juoserver.game.player.template.BodyTemplate;
import com.github.mayconr.juoserver.game.player.template.StartKitTemplate;
import com.github.mayconr.juoserver.game.spell.template.SpellTemplate;
import com.github.mayconr.juoserver.infrastructure.region.RegionTemplate;
import com.github.mayconr.juoserver.infrastructure.template.BaseTemplate;
import com.github.mayconr.juoserver.infrastructure.template.JsonTemplateLoader;
import com.github.mayconr.juoserver.infrastructure.template.JsonTemplateLoaderNew;
import com.github.mayconr.juoserver.infrastructure.template.TemplateLoader;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Owns the shard's on-disk content layout and optional development content. */
public final class ShardContentLoader {
    private ShardContentLoader() {}

    public static WorldContent load(Path root, boolean developmentContent) {
        var content = root.resolve("content");
        var items = new HashMap<>(json(content.resolve("items"), ItemTemplate.class).load());
        if (developmentContent) {
            json(root.resolve("dev/content/items"), ItemTemplate.class).load().forEach((name, item) -> {
                if (items.putIfAbsent(name, item) != null) {
                    throw new IllegalStateException("Duplicate development item: " + name);
                }
            });
        }
        return new WorldContent(
                settings(root.resolve("config")),
                snapshot(items),
                json(content.resolve("npcs"), NpcTemplate.class),
                json(content.resolve("npc-profiles/stats"), NpcStatProfile.class),
                json(content.resolve("npc-profiles/ai"), NpcAIProfile.class),
                new JsonTemplateLoaderNew<>(content.resolve("spells"), SpellTemplate.class),
                json(content.resolve("players/bodies"), BodyTemplate.class),
                json(content.resolve("players/starting-kits"), StartKitTemplate.class),
                json(content.resolve("mounts"), MountTemplate.class),
                json(content.resolve("world/regions"), RegionTemplate.class),
                json(content.resolve("messaging"), MessageStyleTemplate.class),
                json(content.resolve("economy/stocks"), RegionStockTemplate.class)
        );
    }

    private static GamePlaySettings settings(Path config) {
        var mapper = JsonMapper.builder()
                .findAndAddModules()
                .enable(JsonReadFeature.ALLOW_JAVA_COMMENTS)
                .enable(JsonReadFeature.ALLOW_YAML_COMMENTS)
                .build();
        try {
            var gameplay = mapper.readValue(config.resolve("gameplay.json").toFile(), GamePlaySettings.class);
            var server = mapper.readValue(config.resolve("server.json").toFile(), ServerSettings.class);
            // Preserve the engine settings contract while keeping machine settings in a separate file.
            return new GamePlaySettings(gameplay.name(), gameplay.vitals(), server.gameLoop(),
                    gameplay.skills(), gameplay.mobile(), gameplay.world(), server.files(),
                    gameplay.client(), gameplay.economy(), java.util.Objects.requireNonNull(server.ai(),
                    "Server configuration must include ai"));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load shard configuration from " + config, e);
        }
    }

    private static <T extends BaseTemplate> JsonTemplateLoader<T> json(Path path, Class<T> type) {
        return new JsonTemplateLoader<>(path, type);
    }

    private static <T> TemplateLoader<T> snapshot(Map<String, T> entries) {
        var values = Map.copyOf(entries);
        return new TemplateLoader<>() {
            @Override
            public Map<String, T> load() {
                return values;
            }

            @Override
            public List<T> loadAll() {
                return List.copyOf(values.values());
            }
        };
    }

    private record ServerSettings(GamePlaySettings.GameLoop gameLoop, GamePlaySettings.Files files,
                                  GamePlaySettings.Ai ai) {}
}
