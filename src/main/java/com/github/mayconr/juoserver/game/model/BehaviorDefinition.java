package com.github.mayconr.juoserver.game.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonAlias;

// Accept the retired field when loading previously persisted NPCs.
@JsonIgnoreProperties({"profile", "stockType"})
public record BehaviorDefinition(String ai, Integer activationRadius, Integer perceptionRadius,
                                 @JsonAlias("radius") Integer speechRadius,
                                 @JsonAlias("supports") List<String> speechTriggers) {
    public BehaviorDefinition {
        validateRadius("activationRadius", activationRadius);
        validateRadius("perceptionRadius", perceptionRadius);
        validateRadius("speechRadius", speechRadius);
        speechTriggers = speechTriggers == null ? null : List.copyOf(speechTriggers);
    }

    private static void validateRadius(String field, Integer radius) {
        if (radius != null && radius < 0) throw new IllegalArgumentException("AI " + field + " must be non-negative");
    }

}
