package com.github.mayconr.juoserver.game.ai.action;

import com.github.mayconr.juoserver.game.model.Direction;
import com.github.mayconr.juoserver.game.model.UONpc;

public record WalkAction(UONpc npc, Direction direction) implements NpcAction{
}
