package com.github.mayconr.juoserver.common.template;

import com.github.mayconr.juoserver.game.model.Notoriety;
import com.github.mayconr.juoserver.game.model.NpcType;

import java.util.Map;

public record NpcTemplate(String name,
                          String displayName,
                          NpcType type,
                          int modelId,
                          String ai,
                          Notoriety notoriety,
                          int hue,
                          String mountItemName,
                          Map<String, Object> attr) {
}
