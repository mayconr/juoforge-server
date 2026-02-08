package com.github.mayconr.juoserver.game.model.policy;

import com.github.mayconr.juoserver.game.policy.ActionPolicy;
import com.github.mayconr.juoserver.game.model.UOMobile;

public record DoubleClickPolicy(UOMobile mobile, int serialId) implements ActionPolicy {

}
