package com.github.mayconr.juoserver.game.ai.definition.steps;

import com.github.mayconr.juoserver.game.ai.actions.SellListAction;
import com.github.mayconr.juoserver.game.ai.actions.SpeechAction;
import com.github.mayconr.juoserver.game.economy.stock.StockEntry;
import com.github.mayconr.juoserver.game.ai.definition.VendorAIContext;
import com.github.mayconr.juoserver.game.item.template.ItemTemplate;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.event.MobileSpeech;
import com.github.mayconr.juoserver.game.model.event.message.PlainTextMessageContent;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

import java.util.ArrayList;
import java.util.List;

public class VendorStep extends AbstractFlowStep<VendorAIContext> {

    public VendorStep() {
        super("vendor");
    }

    @Override
    public StepResult execute(VendorAIContext ctx) {

        var event = ctx.peekEvent(MobileSpeech.class);

        if (event == null) {
            return StepResult.skip();
        }

        String text = event.message().toLowerCase().trim();

        if (!text.equals("buy")) {
            return StepResult.skip();
        }

        event = ctx.pollEvent(MobileSpeech.class);

        if (event == null) {
            return StepResult.skip();
        }

        var npc = ctx.npc();
        var world = ctx.world();

        var player = (UOPlayer) event.mobile();

        // 2. region
        var region = world.getRegion(npc)
                .orElseThrow(() -> new IllegalStateException(
                        "Region not found for npc [" + npc.getId() + "]"
                ));

        // 3. stock type
        var stockType = ctx.getStockType();

        var templates = world.getItemsTemplate(stockType);

        // 4. create stock
        List<StockEntry> entries = new ArrayList<>();

        for (ItemTemplate template : templates) {
            world.getStockEntry(template, region)
                    .ifPresent(entries::add);
        }

        // 5. action
        if (entries.isEmpty()) {
            ctx.enqueueAction(new SpeechAction(
                    ctx.npc(),
                    player,
                    new PlainTextMessageContent("I have nothing to sell")
            ));
        } else {
            ctx.enqueueAction(new SellListAction(npc, player, entries));
        }

        return StepResult.stop();
    }
}
