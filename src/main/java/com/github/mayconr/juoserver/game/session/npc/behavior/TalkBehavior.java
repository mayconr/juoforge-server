package com.github.mayconr.juoserver.game.session.npc.behavior;

import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.session.npc.NpcContext;
import com.github.mayconr.juoserver.game.session.npc.action.SayAction;

public class TalkBehavior implements NpcBehavior  {

    private NpcContext context;

    @Override
    public void initialize(NpcContext context) {
        this.context = context;
    }

    @Override
    public void onSpeech(UOPlayer player, String text) {
        context.enqueue(new SayAction("Oi"));
    }

    @Override
    public void onThink(double delta) {

    }
}
