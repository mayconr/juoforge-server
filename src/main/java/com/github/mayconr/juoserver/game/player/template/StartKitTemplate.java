package com.github.mayconr.juoserver.game.player.template;

import com.github.mayconr.juoserver.infrastructure.template.BaseTemplate;

public record StartKitTemplate(String name, String item, int amount, Integer skillId) implements BaseTemplate {
    public StartKitTemplate {
        if (skillId == null) {
            skillId = -1;
        }
    }
}
