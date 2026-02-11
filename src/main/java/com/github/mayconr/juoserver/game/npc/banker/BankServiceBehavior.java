package com.github.mayconr.juoserver.game.npc.banker;

import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.npc.NpcContext;
import com.github.mayconr.juoserver.game.npc.action.SellListAction;
import com.github.mayconr.juoserver.game.npc.behavior.NpcBehavior;
import lombok.RequiredArgsConstructor;

import java.util.Collections;

@RequiredArgsConstructor
public class BankServiceBehavior implements NpcBehavior {

    private NpcContext context;

    @Override
    public void initialize(NpcContext context) {
        this.context = context;
    }

    @Override
    public void onSpeech(UOPlayer player, String text) {
        context.enqueue(new SellListAction(player, Collections.emptyList()));
    }

    @Override
    public void onThink(double delta) {

    }
}
