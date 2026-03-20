package com.github.mayconr.juoserver.game.item;

import com.github.mayconr.juoserver.game.item.template.ItemTemplate;

public record ItemRequest(ItemTemplate template, Integer modelId, String itemName, int hue, int amount) {
    public ItemRequest {
        int count = 0;

        if (template != null) count++;
        if (modelId != null) count++;
        if (itemName != null) count++;

        if (count != 1) {
            throw new IllegalArgumentException(
                    "Exactly one of itemName, modelId or template must be provided"
            );
        }
    }

    public static Builder byTemplate(ItemTemplate template) {
        return new Builder().template(template).amount(1);
    }

    public static Builder byName(String itemName) {
        return new Builder().itemName(itemName).amount(1);
    }

    public static Builder byModelId(int modelId) {
        return new Builder().modelId(modelId).amount(1);
    }

    public static class Builder {

        private ItemTemplate template;
        private Integer modelId;
        private String itemName;
        private int hue = 0;
        private int amount = 1;

        private Builder() {
        }

        public Builder template(ItemTemplate template) {
            this.template = template;
            return this;
        }

        public Builder modelId(Integer modelId) {
            this.modelId = modelId;
            return this;
        }

        public Builder itemName(String itemName) {
            this.itemName = itemName;
            return this;
        }

        public Builder hue(int hue) {
            this.hue = hue;
            return this;
        }

        public Builder amount(int amount) {
            this.amount = amount;
            return this;
        }

        public ItemRequest build() {
            return new ItemRequest(
                    template,
                    modelId,
                    itemName,
                    hue,
                    amount
            );
        }
    }
}
