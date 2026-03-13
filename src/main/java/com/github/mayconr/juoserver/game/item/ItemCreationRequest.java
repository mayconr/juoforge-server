package com.github.mayconr.juoserver.game.item;

import com.github.mayconr.juoserver.game.item.template.ItemTemplate;
import lombok.Builder;

@Builder
public record ItemCreationRequest(ItemTemplate template, Integer modelId, String itemName, int hue, int amount) {
    public ItemCreationRequest {
        if ((itemName == null) == (modelId == null) == (template == null)) {
            throw new IllegalArgumentException("Exactly one of itemName, modelId or itemTemplate must be provided");
        }
    }

    public static ItemCreationRequestBuilder byTemplate(ItemTemplate template) {
        return builder().template(template).amount(1);
    }

    public static ItemCreationRequestBuilder byName(String itemName) {
        return builder().itemName(itemName).amount(1);
    }

    public static ItemCreationRequestBuilder byModelId(int modelId) {
        return builder().modelId(modelId).amount(1);
    }
}
