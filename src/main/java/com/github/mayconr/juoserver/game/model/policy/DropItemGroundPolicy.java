package com.github.mayconr.juoserver.game.model.policy;

import com.github.mayconr.juoserver.game.policy.ActionPolicy;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOMobile;

public record DropItemGroundPolicy(UOMobile mobile, UOItem item) implements ActionPolicy {
}
