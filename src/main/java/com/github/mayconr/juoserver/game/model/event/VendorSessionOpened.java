package com.github.mayconr.juoserver.game.model.event;

import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.VendorSession;
import com.github.mayconr.juoserver.infrastructure.eventbus.GameEvent;

public record VendorSessionOpened(UOPlayer player, UOMobile vendor, VendorSession session) implements GameEvent {

}
