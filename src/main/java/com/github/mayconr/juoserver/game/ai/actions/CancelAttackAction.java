package com.github.mayconr.juoserver.game.ai.actions;

import com.github.mayconr.juoserver.game.model.UONpc;

public record CancelAttackAction(UONpc npc) implements NpcAction {}
