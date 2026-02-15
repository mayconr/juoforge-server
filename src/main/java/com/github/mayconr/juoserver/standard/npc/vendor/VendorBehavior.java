package com.github.mayconr.juoserver.standard.npc.vendor;

import com.github.mayconr.juoserver.game.economy.RegionStockEntry;
import com.github.mayconr.juoserver.game.economy.StockType;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.npc.NpcContext;
import com.github.mayconr.juoserver.game.npc.behavior.NpcBehavior;
import com.github.mayconr.juoserver.game.template.definitions.item.ItemTemplate;
import com.github.mayconr.juoserver.game.world.WorldInternal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class VendorBehavior implements NpcBehavior {

    private final WorldInternal world;

    private NpcContext context;

    @Override
    public void initialize(NpcContext context) {
        this.context = context;
    }

    @Override
    public void onSpeech(UOPlayer player, String text) {
        var npc = context.npc();
        var region = world.resolveRegion(context.npc())
                .orElseThrow(() -> new RuntimeException("Region not found"));
        var stockPool = world.getStockPool(region.getName());
        var stockType = StockType.valueOf((String) npc.getPersistentAttribute("behavior.stockType"));
        var templates = world.getItemTemplates(stockType);
        final List<RegionStockEntry> entries = new ArrayList<>();

        for (ItemTemplate  template : templates) {
            var entry = stockPool.getStockEntry(template);
            // There is a stock configured for template + region
            if (entry != null) {
                entries.add(entry);
            }
        }

        world.sendBuyGump(player, npc, entries);

    }

    @Override
    public void onThink(double delta) {

    }
}
