package com.github.mayconr.juoserver.game.economy;

import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.world.WorldModule;
import com.github.mayconr.juoserver.network.packet.VendorBuyRequest;


/** Internal economy module with lifecycle and purchase completion operations. */
public interface EconomyModule extends WorldEconomy, WorldModule {

    /** Runs the purchase flow to debit gold, deliver items and consume session stock. */
    void completeVendorPurchase(UOPlayer player, VendorBuyRequest vendorBuyRequest);

}
