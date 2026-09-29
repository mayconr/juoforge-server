package com.github.mayconr.juoserver.game.npc.stats;

import com.github.mayconr.juoserver.game.mobile.template.NpcStatProfile;
import com.github.mayconr.juoserver.game.mobile.template.NpcTemplate;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/** Explicit NPC values override a single profile. No implicit stat defaults. */
public final class NpcStatsResolver {
    private final Map<String, NpcStatProfile> profiles;

    public NpcStatsResolver(Collection<NpcStatProfile> profiles) {
        var byName = new HashMap<String, NpcStatProfile>();
        for (var profile : profiles) {
            if (byName.putIfAbsent(profile.name(), profile) != null) {
                throw new IllegalArgumentException("Duplicate NPC stat profile: " + profile.name());
            }
        }
        this.profiles = Map.copyOf(byName);
    }

    public NpcStats resolve(NpcTemplate npc) {
        NpcStatProfile profile = null;
        if (npc.statProfile() != null) {
            profile = profiles.get(npc.statProfile());
            if (profile == null) {
                throw new IllegalArgumentException("NPC '" + npc.name()
                        + "': unknown stat profile '" + npc.statProfile() + "'");
            }
        }
        return new NpcStats(
                value(npc, "strength", npc.strength(), profile == null ? null : profile.strength()),
                value(npc, "dexterity", npc.dexterity(), profile == null ? null : profile.dexterity()),
                value(npc, "intelligence", npc.intelligence(), profile == null ? null : profile.intelligence()),
                value(npc, "maxHitpoints", npc.maxHitpoints(), profile == null ? null : profile.maxHitpoints()),
                value(npc, "maxStamina", npc.maxStamina(), profile == null ? null : profile.maxStamina()),
                value(npc, "maxMana", npc.maxMana(), profile == null ? null : profile.maxMana()));
    }

    private int value(NpcTemplate npc, String field, Integer override, Integer inherited) {
        var result = override != null ? override : inherited;
        if (result == null || result <= 0) {
            throw new IllegalArgumentException("NPC '" + npc.name() + "': " + field + " must be provided and positive");
        }
        return result;
    }
}
