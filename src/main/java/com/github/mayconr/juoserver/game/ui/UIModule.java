package com.github.mayconr.juoserver.game.ui;

import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.ui.contextmenu.ContextMenuEntry;
import java.util.List;
import com.github.mayconr.juoserver.game.world.WorldModule;
import com.github.mayconr.juoserver.network.packet.DoubleClick;
import com.github.mayconr.juoserver.network.packet.GumpSelection;
import com.github.mayconr.juoserver.network.packet.SingleClickRequest;


/** Internal UI operations and lifecycle, extending the public world UI API. */
public interface UIModule extends WorldUI, WorldModule {
    void registerContextMenuEntries(List<ContextMenuEntry> entries);

    void requestContextMenu(UOPlayer player, int serialId);

    void onContextMenuSelection(UOPlayer player, int serialId, int index);

    void doubleClick(UOPlayer player, DoubleClick doubleClick);

    void singleClick(UOPlayer player, SingleClickRequest request);

    void onGumpSelection(UOPlayer player, GumpSelection gumpSelection);
}
