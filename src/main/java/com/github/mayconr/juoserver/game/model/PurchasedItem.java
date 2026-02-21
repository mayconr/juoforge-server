package com.github.mayconr.juoserver.game.model;

import com.github.mayconr.juoserver.game.item.template.ItemTemplate;

public record PurchasedItem(ItemTemplate template, int amount) {

}
