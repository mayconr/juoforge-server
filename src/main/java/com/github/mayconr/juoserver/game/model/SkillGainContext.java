package com.github.mayconr.juoserver.game.model;

import java.util.Collections;
import java.util.Map;

public record SkillGainContext(Map<String, Object> attributes) {
    public static SkillGainContext empty() {
        return new SkillGainContext(Collections.emptyMap());
    }
}
