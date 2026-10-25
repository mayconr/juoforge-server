package com.github.mayconr.juoserver.game.economy.flow.purchase;

import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractContext;
import com.github.mayconr.juoserver.network.packet.VendorBuyRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class VendorPurchaseContext extends AbstractContext {
    private final UOPlayer player;
    private final VendorBuyRequest request;
    private final VendorSession session;
    private final List<UOItem> items = new ArrayList<>();
    @Setter private UOContainer backpack;
    @Setter private VendorPurchaseResult purchase;
}

