package com.github.mayconr.juoserver.game.economy;

import com.github.mayconr.juoserver.game.economy.template.RegionStockTemplate;
import com.github.mayconr.juoserver.game.economy.template.StockTemplate;
import com.github.mayconr.juoserver.game.item.template.ItemTemplate;
import com.github.mayconr.juoserver.game.item.template.ItemTemplateRegistry;
import com.github.mayconr.juoserver.game.model.RegionType;
import com.github.mayconr.juoserver.game.world.context.DefaultModuleContext;
import com.github.mayconr.juoserver.game.world.context.ModuleContext;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.region.RectangularArea;
import com.github.mayconr.juoserver.infrastructure.region.RegionNode;
import com.github.mayconr.juoserver.infrastructure.template.TemplateLoader;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EconomyModuleInitializationTest {
    @Test
    void contextInitializationLoadsStocksAndResolvesParentRegion() {
        var registry = mock(ItemTemplateRegistry.class);
        var pickaxe = mock(ItemTemplate.class);
        when(registry.get("pickaxe")).thenReturn(pickaxe);
        TemplateLoader<RegionStockTemplate> loader = new TemplateLoader<>() {
            @Override
            public Map<String, RegionStockTemplate> load() {
                return Map.of("moonglow-stock", new RegionStockTemplate("moonglow-stock", "moonglow-city",
                        List.of(new StockTemplate("pickaxe", 3, 2, 1.2))));
            }
        };
        var module = new EconomyModuleImpl(mock(EventBus.class), new StockHandler(), loader, registry);
        var city = new RegionNode("moonglow-city", "Moonglow", RegionType.CITY,
                new RectangularArea(0, 0, 100, 100));
        var starting = new RegionNode("moonglow-starting", "Starting", RegionType.STARTING_LOCATION,
                new RectangularArea(0, 0, 5, 5));
        city.addChild(starting);
        assertTrue(module.getStockEntry(pickaxe, starting).isEmpty());

        module.initialize(DefaultModuleContext.builder()
                .flowFacade(mock(ModuleContext.FlowFacade.class)).build());

        var stock = module.getStockEntry(pickaxe, starting).orElseThrow();
        assertSame(pickaxe, stock.getItemTemplate());
        assertEquals(2, stock.getCurrentStock());
        assertEquals(3, stock.getMaxStock());
        assertSame(stock, module.getStockEntry(pickaxe, city).orElseThrow());
    }
}
