package com.github.mayconr.juoserver.infrastructure.storage;

import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UONpc;

import java.util.List;

public record InitialData(List<UONpc> npcs, List<UOItem> items) {
}
