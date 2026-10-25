package com.github.mayconr.juoserver.game.economy.flow.begin;

import com.github.mayconr.juoserver.game.economy.stock.StockEntry;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractContext;
import com.github.mayconr.juoserver.infrastructure.region.RegionNode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class BeginVendorPurchaseContext extends AbstractContext {
    private final UOPlayer player;
    private final UOMobile vendor;
    private final RegionNode region;
    private final List<StockEntry> items;
    @Setter private VendorSession session;
}

