package com.github.mayconr.juoserver.game.npc.ai;

public class NpcAiFactory {

    public NpcAI create(String name) {
        System.out.println("creating AI for "+name);
        return new DialogueReactiveAI();
    }

}
