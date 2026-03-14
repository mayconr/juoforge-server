package com.github.mayconr.juoserver.game.player.template;

import com.github.mayconr.juoserver.infrastructure.template.BaseTemplate;

public record StartkitTemplate(String name, String item, int amount, Integer skillId) implements BaseTemplate {
    public StartkitTemplate {
        if (skillId == null) {
            skillId = -1;
        }
    }
}
