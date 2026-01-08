package com.github.mayconr.juoserver.game.core.session.game;

import com.github.mayconr.juoserver.game.core.model.Location;
import com.github.mayconr.juoserver.game.core.model.UOItem;
import com.github.mayconr.juoserver.game.core.model.UOMobile;
import com.github.mayconr.juoserver.game.core.model.UOPlayer;
import com.github.mayconr.juoserver.game.core.session.npc.NpcSession;
import com.github.mayconr.juoserver.game.core.session.player.PlayerSession;

import io.netty.channel.ChannelHandlerContext;

public interface GameSession {

    void sendBroadcastMessage(String message);

    PlayerSession getPlayerSession(UOMobile mobile);

    NpcSession createNpcSession(String name, Location location);

    PlayerSession createPlayerSession(UOPlayer player, ChannelHandlerContext ctx);

    UOItem createItemAtLocation(String name, Location location);

    void deleteItem(UOItem item);

    void moveItem(UOItem item, Location location);
}
