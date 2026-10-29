package com.github.mayconr.juoserver.game.mobile.shared;

import com.github.mayconr.juoserver.game.model.UOMobile;

public interface ValidateBackpackWeightContext {
    UOMobile mobile();
    /** Net weight to add; callers account for stack amounts and nested contents. */
    int addedWeight();
}
