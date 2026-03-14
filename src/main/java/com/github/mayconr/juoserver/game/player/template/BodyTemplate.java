package com.github.mayconr.juoserver.game.player.template;

import com.github.mayconr.juoserver.game.model.Gender;
import com.github.mayconr.juoserver.game.model.Race;
import com.github.mayconr.juoserver.infrastructure.template.BaseTemplate;

import java.util.List;

public record BodyTemplate(String name,
                           Race race,
                           Gender gender,
                           int modelId,
                           Integer ghostModelId,
                           List<Integer> defaultSkinHueRange) implements BaseTemplate {
}
