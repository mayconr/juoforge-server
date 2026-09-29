package com.github.mayconr.shard;

import com.github.mayconr.juoserver.infrastructure.region.RegionSystemImpl;
import com.github.mayconr.juoserver.game.npc.stats.NpcStats;
import com.github.mayconr.juoserver.game.npc.stats.NpcStatsResolver;
import com.github.mayconr.juoserver.infrastructure.template.JsonTemplateLoaderNew;
import com.github.mayconr.shard.skills.crafting.mining.Ore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ShardContentLoaderTest {
    @Test
    void loadsShippedContentWithValidReferences() {
        var content = ShardContentLoader.load(Path.of("."), false);
        var items = content.items().load();
        var npcs = content.npcs().load();
        var statsResolver = new NpcStatsResolver(content.npcStatProfiles().loadAll());
        var regions = new RegionSystemImpl(content.regions());

        assertEquals(20, content.settings().gameLoop().tps());
        assertNotNull(content.settings().files().dataFileRoot());
        assertEquals(50, content.settings().skills().cap());
        assertTrue(items.containsKey(content.settings().mobile().backpackItem()));
        assertTrue(items.containsKey(content.settings().economy().goldCoinItem()));
        assertFalse(items.containsKey("marker"));
        assertFalse(items.containsKey("marker_flag"));
        assertEquals(64, content.spells().loadAll().size());
        assertEquals(4, content.bodies().loadAll().size());
        assertTrue(content.messageStyles().load().containsKey("SYSTEM"));

        for (var kit : content.startingKits().loadAll()) {
            assertTrue(items.containsKey(kit.item()), kit.item());
        }
        for (var npc : npcs.values()) {
            var stats = statsResolver.resolve(npc);
            int expected = npc.name().equals("skeleton") ? 50 : 100;
            assertEquals(new NpcStats(expected, expected, expected, expected, expected, expected), stats);
            for (var item : npc.equippedItems()) {
                assertTrue(items.containsKey(item), item);
            }
        }
        for (var mount : content.mounts().loadAll()) {
            assertTrue(npcs.containsKey(mount.npcName()), mount.npcName());
            assertTrue(items.containsKey(mount.itemName()), mount.itemName());
        }
        for (var stock : content.stocks().loadAll()) {
            assertTrue(regions.getRegion(stock.region()).isPresent(), stock.region());
            for (var entry : stock.initialStock()) {
                assertTrue(items.containsKey(entry.itemName()), entry.itemName());
            }
        }
        for (var ore : new JsonTemplateLoaderNew<>(Path.of("content/skills/mining"), Ore.class).loadAll()) {
            assertTrue(items.containsKey(ore.itemName()), ore.itemName());
        }
    }

    @Test
    void loadsDevelopmentItemsOnlyWhenEnabled() {
        var normal = ShardContentLoader.load(Path.of("."), false).items().load();
        var development = ShardContentLoader.load(Path.of("."), true).items().load();
        assertEquals(normal.size() + 2, development.size());
        assertEquals(normal, development.entrySet().stream()
                .filter(entry -> normal.containsKey(entry.getKey()))
                .collect(java.util.stream.Collectors.toMap(java.util.Map.Entry::getKey, java.util.Map.Entry::getValue)));
        assertTrue(development.containsKey("marker"));
        assertTrue(development.containsKey("marker_flag"));
    }

    @Test
    void resolvesContentFromConfiguredRootAndRejectsDevelopmentDuplicates(@TempDir Path root) throws IOException {
        for (var directory : new String[]{"config", "content"}) {
            var source = Path.of(directory);
            try (var paths = Files.walk(source)) {
                for (var path : paths.toList()) {
                    var destination = root.resolve(directory).resolve(source.relativize(path));
                    if (Files.isDirectory(path)) {
                        Files.createDirectories(destination);
                    } else {
                        Files.copy(path, destination);
                    }
                }
            }
        }
        assertEquals(64, ShardContentLoader.load(root, false).spells().loadAll().size());
        var devItems = root.resolve("dev/content/items");
        Files.createDirectories(devItems);
        Files.writeString(devItems.resolve("duplicate.json"), "[{\"name\":\"backpack\",\"modelId\":3701}]");
        var error = assertThrows(IllegalStateException.class, () -> ShardContentLoader.load(root, true));
        assertTrue(error.getMessage().contains("backpack"));
    }
}
