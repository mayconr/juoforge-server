package com.github.mayconr.juoserver.common.policy;

public interface ActionPolicy<A> {

    PolicyResult evaluate(A action, ActionContext ctx);

}
