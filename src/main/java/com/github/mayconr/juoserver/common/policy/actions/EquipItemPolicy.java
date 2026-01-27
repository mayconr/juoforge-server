package com.github.mayconr.juoserver.common.policy.actions;

import com.github.mayconr.juoserver.common.policy.ActionPolicy;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOMobile;

public record EquipItemPolicy(UOMobile mobile, UOItem item) implements ActionPolicy {
}
