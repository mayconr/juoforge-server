package com.github.mayconr.juoserver.game.ai.behaviors;

import com.github.mayconr.juoserver.game.ai.AIContext;
import com.github.mayconr.juoserver.game.ai.actions.SellListAction;
import com.github.mayconr.juoserver.game.ai.Behavior;
import com.github.mayconr.juoserver.game.economy.stock.StockEntry;
import com.github.mayconr.juoserver.game.economy.stock.StockType;
import com.github.mayconr.juoserver.game.item.template.ItemTemplate;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class VendorBehavior implements Behavior {

    private AIContext context;

    @Override
    public void initialize(AIContext context) {
        this.context = context;
    }

    @Override
    public void onSpeech(UOPlayer player, String text) {
        var npc = context.npc();
        var world = context.world();

        var region = world.resolveRegion(context.npc())
                .orElseThrow(() -> new RuntimeException("Region not found"));
        var stockType = StockType.valueOf((String) npc.getPersistentAttribute("behavior.stockType"));
        var templates = world.getItemsTemplate(stockType);
        final List<StockEntry> entries = new ArrayList<>();
        for (ItemTemplate template : templates) {
            world.getStockEntry(template, region)
                    .ifPresent(entries::add);
        }
        context.enqueue(new SellListAction(player, entries));
    }

    @Override
    public void onThink(double delta) {

    }
}
