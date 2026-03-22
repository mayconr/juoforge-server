package com.github.mayconr.juoserver.game.ai.behaviors;

import com.github.mayconr.juoserver.game.ai.AIContext;
import com.github.mayconr.juoserver.game.ai.Behavior;
import com.github.mayconr.juoserver.game.ai.actions.SellListAction;
import com.github.mayconr.juoserver.game.ai.actions.SpeechAction;
import com.github.mayconr.juoserver.game.economy.stock.StockEntry;
import com.github.mayconr.juoserver.game.item.template.ItemTemplate;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.event.message.PlainTextMessageContent;
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

        final var region = world.getRegion(context.npc())
                .orElseThrow(() -> new RuntimeException("Region not found for npc ["+npc.getId()+"]"));

        final var stockType = (String) npc.persistentAttributes().get("behavior.stockType");
        final var templates = world.getItemsTemplate(stockType);

        final List<StockEntry> entries = new ArrayList<>();
        for (ItemTemplate template : templates) {
            world.getStockEntry(template, region)
                    .ifPresent(entries::add);
        }

        if (entries.isEmpty()) {
            context.enqueue(new SpeechAction(new PlainTextMessageContent("I have nothing to sell"), player));
        } else {
            context.enqueue(new SellListAction(player, entries));
        }
    }

    @Override
    public void onThink(double delta) {

    }
}
