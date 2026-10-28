package com.github.mayconr.juoserver.game.region.shared;

import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.UOMobile;

public interface UpdateMobileRegionContext {
    UOMobile mobile();
    Location regionFrom();
    Location regionTo();
}
