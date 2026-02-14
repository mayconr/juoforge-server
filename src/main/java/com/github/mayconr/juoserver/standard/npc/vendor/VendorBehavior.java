package com.github.mayconr.juoserver.standard.npc.vendor;

import com.github.mayconr.juoserver.game.economy.StockType;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.npc.NpcContext;
import com.github.mayconr.juoserver.game.npc.behavior.NpcBehavior;
import com.github.mayconr.juoserver.game.template.definitions.item.ItemTemplate;
import com.github.mayconr.juoserver.game.world.WorldInternal;
import lombok.RequiredArgsConstructor;

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
        for (ItemTemplate  template : templates) {
            double price = world.getPrice(template, region.getName());
            System.out.println(template.name() + ": " + price);
        }

        //System.out.println(stockPool.getStockEntry().getCurrentStock());

    }

    @Override
    public void onThink(double delta) {

    }
}
