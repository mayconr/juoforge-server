package com.github.mayconr.juoserver.game.world.vendor;

import com.github.mayconr.juoserver.game.event.EventBus;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.event.BuyGumpSent;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class VendorService {

    private final EventBus eventBus;

    public void sendBuyGump(UOPlayer player, UOMobile vendor, List<UOItem> items) {
        eventBus.publish(new BuyGumpSent(player, vendor, items));
    }
}
