package com.github.mayconr.juoserver.game.economy.template;

import com.github.mayconr.juoserver.infrastructure.template.BaseTemplate;

import java.util.List;

public record RegionStockTemplate(String name, String region, List<StockTemplate> initialStock) implements BaseTemplate {

}
