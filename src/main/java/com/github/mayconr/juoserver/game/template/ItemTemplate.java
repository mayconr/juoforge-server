package com.github.mayconr.juoserver.game.template;

import com.github.mayconr.juoserver.game.model.ItemFlag;
import com.github.mayconr.juoserver.game.model.Layer;

import java.util.List;
import java.util.Map;

public record ItemTemplate(String name,
                           String displayName,
                           Layer layer,
                           int modelId,
                           boolean movable,
                           int hue,
                           String mountNpc,
                           List<ItemFlag> flags,
                           Map<String, Object> attr)
            implements BaseTemplate{
}
