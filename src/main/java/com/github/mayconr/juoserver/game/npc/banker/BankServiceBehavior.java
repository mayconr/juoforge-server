package com.github.mayconr.juoserver.game.npc.banker;

import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.npc.NpcContext;
import com.github.mayconr.juoserver.game.npc.action.BuyListAction;
import com.github.mayconr.juoserver.game.npc.behavior.NpcBehavior;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BankServiceBehavior implements NpcBehavior {

    private NpcContext context;

    @Override
    public void initialize(NpcContext context) {
        this.context = context;
    }

    @Override
    public void onSpeech(UOPlayer player, String text) {
        context.enqueue(new BuyListAction(player));
    }

    @Override
    public void onThink(double delta) {

    }
}
