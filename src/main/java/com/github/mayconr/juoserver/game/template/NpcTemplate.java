package com.github.mayconr.juoserver.game.template;

import com.github.mayconr.juoserver.game.model.BehaviorDefinition;
import com.github.mayconr.juoserver.game.model.Notoriety;
import com.github.mayconr.juoserver.game.model.NpcType;

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
                          Map<String, Object> attr,
                          List<String> equippedItems)
        implements BaseTemplate {

}
