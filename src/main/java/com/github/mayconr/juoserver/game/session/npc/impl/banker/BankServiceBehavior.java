package com.github.mayconr.juoserver.game.session.npc.impl.banker;

import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.session.npc.NpcContext;
import com.github.mayconr.juoserver.game.session.npc.action.BuyListAction;
import com.github.mayconr.juoserver.game.session.npc.behavior.NpcBehavior;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class BankServiceBehavior implements NpcBehavior {

    private NpcContext context;

    @Override
    public void initialize(NpcContext context) {
        this.context = context;
    }

    @Override
    public void onSpeech(UOPlayer player, String text) {
        var item = context.world().createItemAtLocation("robe", player);
        context.enqueue(new BuyListAction(player, List.of(item)));
    }

    @Override
    public void onThink(double delta) {

    }
}
