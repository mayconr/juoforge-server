package com.github.mayconr.juoserver.game.session.game;

import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.session.npc.NpcSession;
import com.github.mayconr.juoserver.game.session.player.PlayerSession;

import com.github.mayconr.juoserver.game.session.SessionOutbound;
import io.netty.channel.ChannelHandlerContext;

public interface GameSession {

    void sendBroadcastMessage(String message);

    PlayerSession getPlayerSession(UOMobile mobile);

    NpcSession createNpcSession(String name, Location location);

    PlayerSession createPlayerSession(UOPlayer player, ChannelHandlerContext ctx, SessionOutbound outbound);

    UOItem createItemAtLocation(String name, Location location);

    void deleteItem(UOItem item);

    void moveItem(UOItem item, Location location);
}
