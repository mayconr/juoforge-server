package com.github.mayconr.juoserver.standard.ai.npc.banker;

import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.ai.AIContext;
import com.github.mayconr.juoserver.game.ai.action.SellListAction;
import com.github.mayconr.juoserver.game.ai.behavior.Behavior;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;

@Slf4j
@RequiredArgsConstructor
public class BankBehavior implements Behavior {

    private AIContext context;

    @Override
    public void initialize(AIContext context) {
        this.context = context;
    }

    @Override
    public void onSpeech(UOPlayer player, String text) {
        if (log.isDebugEnabled()) {
            log.debug("Palyer {} - onSpeech: {}" , player.getName(), text);
        }
        context.enqueue(new SellListAction(player, Collections.emptyList()));
    }

    @Override
    public void onThink(double delta) {

    }
}
