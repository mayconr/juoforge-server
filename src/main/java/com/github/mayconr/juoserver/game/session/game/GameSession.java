package com.github.mayconr.juoserver.game.session.game;

import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.session.SessionOutbound;
import com.github.mayconr.juoserver.game.session.npc.NpcSession;
import com.github.mayconr.juoserver.game.session.player.PlayerSession;

import java.util.concurrent.CompletableFuture;

public interface GameSession {

    void sendBroadcastMessage(String message);

    PlayerSession getPlayerSession(UOMobile mobile);

    CompletableFuture<NpcSession> createNpcSession(String name, Location location);

    PlayerSession createPlayerSession(UOPlayer player, SessionOutbound outbound);

    CompletableFuture<UOItem> createItemAtLocation(String name, Location location);

    void deleteItem(UOItem item);

    void moveItem(UOItem item, Location location);

    void initialize();
}
