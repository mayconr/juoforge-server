package com.github.mayconr.juoserver.game.mobile.template;

import com.github.mayconr.juoserver.game.model.BehaviorDefinition;
import com.github.mayconr.juoserver.infrastructure.template.BaseTemplate;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonAlias;

/** Explicit AI parameters shared by NPC templates. */
@JsonIgnoreProperties({"profile", "stockType"})
public record NpcAIProfile(String name, String ai, Integer activationRadius, Integer perceptionRadius,
                           @JsonAlias("radius") Integer speechRadius,
                           @JsonAlias("supports") List<String> speechTriggers) implements BaseTemplate {
    public NpcAIProfile {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("NPC AI profile name is required");
        if (ai == null || ai.isBlank()) throw new IllegalArgumentException("NPC AI profile type is required");
        new BehaviorDefinition(ai, activationRadius, perceptionRadius, speechRadius, speechTriggers);
        speechTriggers = speechTriggers == null ? List.of() : List.copyOf(speechTriggers);
    }

    public BehaviorDefinition behavior() {
        return new BehaviorDefinition(ai, activationRadius, perceptionRadius, speechRadius, speechTriggers);
    }
}
