package com.github.mayconr.juoserver.game.model;

import java.util.Collections;
import java.util.Map;

public record SkillGainContext(Location location, Map<String, Object> attributes) {

    public static SkillGainContext of(Location location) {
        return new SkillGainContext(location, Collections.emptyMap());
    }

}
