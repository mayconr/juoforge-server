package com.github.mayconr.juoserver.common.policy.actions;

import com.github.mayconr.juoserver.common.policy.ActionPolicy;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UONpc;

public record Mount(UOMobile mobile, UONpc npc) implements ActionPolicy {
}
