package com.github.mayconr.juoserver.game.ai.actions;

import com.github.mayconr.juoserver.game.model.UONpc;

public record AttackAction(UONpc npc, int targetSerial) implements NpcAction {}
