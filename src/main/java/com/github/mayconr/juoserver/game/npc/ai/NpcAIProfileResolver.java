package com.github.mayconr.juoserver.game.npc.ai;

import com.github.mayconr.juoserver.game.mobile.template.NpcAIProfile;
import com.github.mayconr.juoserver.game.mobile.template.NpcTemplate;
import com.github.mayconr.juoserver.game.model.BehaviorDefinition;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/** Inline behavior values override one profile, including an explicitly empty speechTriggers list. */
public final class NpcAIProfileResolver {
    private final Map<String, NpcAIProfile> profiles;

    public NpcAIProfileResolver(Collection<NpcAIProfile> profiles) {
        var byName = new HashMap<String, NpcAIProfile>();
        for (var profile : profiles) {
            if (byName.putIfAbsent(profile.name(), profile) != null) {
                throw new IllegalArgumentException("Duplicate NPC AI profile: " + profile.name());
            }
        }
        this.profiles = Map.copyOf(byName);
    }

    public BehaviorDefinition resolve(NpcTemplate npc) {
        BehaviorDefinition inherited = null;
        if (npc.aiProfile() != null) {
            var profile = profiles.get(npc.aiProfile());
            if (profile == null) throw new IllegalArgumentException("NPC '" + npc.name()
                    + "': unknown AI profile '" + npc.aiProfile() + "'");
            inherited = profile.behavior();
        }
        var inline = npc.behavior();
        if (inline == null && inherited == null) {
            throw new IllegalArgumentException("NPC '" + npc.name() + "': AI profile or behavior is required");
        }
        var result = new BehaviorDefinition(
                value(inline == null ? null : inline.ai(), inherited == null ? null : inherited.ai()),
                value(inline == null ? null : inline.activationRadius(), inherited == null ? null : inherited.activationRadius()),
                value(inline == null ? null : inline.perceptionRadius(), inherited == null ? null : inherited.perceptionRadius()),
                value(inline == null ? null : inline.speechRadius(), inherited == null ? null : inherited.speechRadius()),
                value(inline == null ? null : inline.speechTriggers(), inherited == null ? java.util.List.of() : inherited.speechTriggers()));
        if (result.ai() == null || result.ai().isBlank()) {
            throw new IllegalArgumentException("NPC '" + npc.name() + "': AI type is required");
        }
        return result;
    }

    private <T> T value(T override, T inherited) {
        return override != null ? override : inherited;
    }
}
