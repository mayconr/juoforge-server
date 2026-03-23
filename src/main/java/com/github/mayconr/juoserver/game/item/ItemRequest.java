package com.github.mayconr.juoserver.game.item;

import com.github.mayconr.juoserver.game.item.template.ItemTemplate;
import com.github.mayconr.juoserver.game.model.Direction;

public record ItemRequest(
        ItemTemplate template,
        Integer modelId,
        String name,
        int hue,
        int amount,
        Direction direction
) {
    public ItemRequest {
        int count = 0;

        if (template != null) count++;
        if (modelId != null) count++;
        if (name != null && !name.isBlank()) count++;

        if (count != 1) {
            throw new IllegalArgumentException("Exactly one of template, modelId or name must be provided");
        }

        if (amount <= 0) {
            throw new IllegalArgumentException("amount must be greater than zero");
        }
    }

    public static ItemRequest byTemplate(ItemTemplate template) {
        return new ItemRequest(template, null, null, 0, 1, null);
    }

    public static ItemRequest byModelId(int modelId) {
        return new ItemRequest(null, modelId, null, 0, 1, null);
    }

    public static ItemRequest byName(String name) {
        return new ItemRequest(null, null, name, 0, 1, null);
    }

    public ItemRequest withHue(int hue) {
        return new ItemRequest(template, modelId, name, hue, amount, direction);
    }

    public ItemRequest withAmount(int amount) {
        return new ItemRequest(template, modelId, name, hue, amount, direction);
    }

    public ItemRequest withDirection(Direction direction) {
        return new ItemRequest(template, modelId, name, hue, amount, direction);
    }
}
