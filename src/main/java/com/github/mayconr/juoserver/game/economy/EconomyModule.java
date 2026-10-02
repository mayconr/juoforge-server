package com.github.mayconr.juoserver.game.economy;

import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.VendorPurchaseResult;
import com.github.mayconr.juoserver.game.world.WorldModule;
import com.github.mayconr.juoserver.network.packet.VendorBuyRequest;


public interface EconomyModule extends WorldEconomy, WorldModule {

    void completeVendorPurchase(UOPlayer player, VendorBuyRequest vendorBuyRequest);

    VendorPurchaseResult resolveVendorPurchase(UOPlayer player, VendorBuyRequest vendorBuyRequest);

}
