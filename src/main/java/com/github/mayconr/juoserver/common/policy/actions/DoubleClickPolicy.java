package com.github.mayconr.juoserver.common.policy.actions;

import com.github.mayconr.juoserver.common.policy.ActionPolicy;
import com.github.mayconr.juoserver.game.model.UOMobile;

public record DoubleClickPolicy(UOMobile mobile, int serialId) implements ActionPolicy {

}
