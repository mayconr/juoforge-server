package com.github.mayconr.juoserver.game.policy;

public interface Policy<A extends ActionPolicy> {

    PolicyResult evaluate(A action);

}
