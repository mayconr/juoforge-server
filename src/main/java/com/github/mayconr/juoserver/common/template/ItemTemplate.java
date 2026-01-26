package com.github.mayconr.juoserver.common.template;

import com.github.mayconr.juoserver.game.model.ItemType;
import com.github.mayconr.juoserver.game.model.Layer;

import java.util.Map;

public record ItemTemplate(String name,
                           String displayName,
                           ItemType type,
                           Layer layer,
                           int modelId,
                           boolean movable,
                           int hue,
                           String mountNpc,
                           Map<String, Object> attr) {
}
