package com.github.mayconr.juoserver.game.npc.ai;

public class NpcAiFactory {

    public NpcAI create(String name) {
        return new DialogueReactiveAI();
    }

}
