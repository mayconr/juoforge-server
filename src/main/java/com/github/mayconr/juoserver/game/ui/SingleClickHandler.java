package com.github.mayconr.juoserver.game.ui;

import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import com.github.mayconr.juoserver.network.packet.SingleClickRequest;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SingleClickHandler {

    private final RealmStorage storage;

    public void singleClick(UOPlayer player, SingleClickRequest request) {
        final int serial = request.getSerialId();
        if (UOMobile.isMobile(serial)) {

        } else if (UOItem.isItem(serial)) {
            storage.getItem(serial)
                    .ifPresent(item->{
                        System.out.println("single lcick");
                        //outbound.writeAndFlush(new ObjectInfo(item));
                    });
        }

    }
}
