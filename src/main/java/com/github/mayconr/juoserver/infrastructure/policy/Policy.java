package com.github.mayconr.juoserver.infrastructure.policy;

public interface Policy<A extends ActionPolicy> {

    PolicyResult evaluate(A action);

}
