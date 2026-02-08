package com.github.mayconr.juoserver.game.session.player.vendor;

import com.github.mayconr.juoserver.game.model.Layer;
import com.github.mayconr.juoserver.game.model.UOContainer;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.session.SessionOutbound;
import com.github.mayconr.juoserver.network.packet.AddMultipleItemsToContainer;
import com.github.mayconr.juoserver.network.packet.DrawContainer;
import com.github.mayconr.juoserver.network.packet.EquipItem;
import com.github.mayconr.juoserver.network.packet.VendorBuyList;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
public class VendorService {

    private final SessionOutbound outbound;

    public void sendBuyList(UOMobile vendor, List<UOItem> items) {
        final var sellContainer = (UOContainer) vendor.getEquippedItems().get(Layer.SHOP_SALE);
        final var boughtContainer = (UOContainer) vendor.getEquippedItems().get(Layer.SHOP_BOUGHT);
        final var packContainer = (UOContainer) vendor.getEquippedItems().get(Layer.SHOP_PACK);
        outbound.write(new EquipItem(vendor, Layer.SHOP_SALE, sellContainer));
        outbound.write(new EquipItem(vendor, Layer.SHOP_BOUGHT, boughtContainer));
        //outbound.write(new EquipItem(vendor, Layer.SHOP_PACK, packContainer));
        outbound.write(new AddMultipleItemsToContainer(sellContainer, items));
        outbound.write(new AddMultipleItemsToContainer(boughtContainer, Collections.emptyList()));
        outbound.write(new VendorBuyList(vendor, items));
        outbound.write(new DrawContainer(vendor));
        outbound.flush();
    }

}
