package com.github.mayconr.juoserver.game.world;

import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.network.packet.DoubleClick;
import com.github.mayconr.juoserver.network.packet.SingleClickRequest;

public interface ClickInternal {

    void doubleClick(UOPlayer player, DoubleClick doubleClick);

    void singleClick(UOPlayer player, SingleClickRequest singleClick);
}
