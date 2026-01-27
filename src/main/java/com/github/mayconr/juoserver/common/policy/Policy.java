package com.github.mayconr.juoserver.common.policy;

public interface Policy<A extends ActionPolicy> {

    PolicyResult evaluate(A action);

}
