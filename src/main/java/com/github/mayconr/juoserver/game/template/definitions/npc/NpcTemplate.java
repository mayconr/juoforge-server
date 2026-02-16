package com.github.mayconr.juoserver.game.template.definitions.npc;

import com.github.mayconr.juoserver.game.model.BehaviorDefinition;
import com.github.mayconr.juoserver.game.model.Notoriety;
import com.github.mayconr.juoserver.game.model.NpcType;
import com.github.mayconr.juoserver.infrastructure.template.BaseTemplate;

import java.util.List;
import java.util.Map;

public record NpcTemplate(String name,
                          String displayName,
                          NpcType type,
                          int modelId,
                          Notoriety notoriety,
                          int hue,
                          String mountItemName,
                          BehaviorDefinition behavior,
                          List<String> roles,
                          Map<String, Object> attr,
                          List<String> equippedItems)
        implements BaseTemplate {

}
