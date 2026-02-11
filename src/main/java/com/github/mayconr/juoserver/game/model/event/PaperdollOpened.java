package com.github.mayconr.juoserver.game.model.event;

import com.github.mayconr.juoserver.game.event.GameEvent;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.network.packet.OpenPaperdoll;

public record PaperdollOpened(UOPlayer player, UOMobile paperdoll, OpenPaperdoll.Flag flag) implements GameEvent {
}
