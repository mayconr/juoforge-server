package com.github.mayconr.juoserver.game.ui;

import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.network.packet.DoubleClick;
import com.github.mayconr.juoserver.network.packet.GumpSelection;
import com.github.mayconr.juoserver.network.packet.SingleClickRequest;

import java.util.List;

public interface UICommands extends WorldUI {

    void tooltipRequest(UOPlayer player, List<Integer> serials);

    void doubleClick(UOPlayer player, DoubleClick doubleClick);

    void singleClick(UOPlayer player, SingleClickRequest request);

    void onGumpSelection(UOPlayer player, GumpSelection gumpSelection);

}
