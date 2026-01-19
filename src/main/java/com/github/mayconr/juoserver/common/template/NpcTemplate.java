package com.github.mayconr.juoserver.common.template;

import com.github.mayconr.juoserver.game.model.Notoriety;
import com.github.mayconr.juoserver.game.model.NpcType;

import java.util.Map;

public record NpcTemplate(String name,
                          NpcType type,
                          int modelId,
                          String ai,
                          Notoriety notoriety,
                          int hue,
                          Map<String, Object> attrMap) {
}
