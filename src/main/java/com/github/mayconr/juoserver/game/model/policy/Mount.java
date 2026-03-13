package com.github.mayconr.juoserver.game.model.policy;

import com.github.mayconr.juoserver.infrastructure.policy.ActionPolicy;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UONpc;

public record Mount(UOMobile mobile, UONpc npc) implements ActionPolicy {
}
