package com.github.mayconr.juoserver.game.loot;

import com.github.mayconr.juoserver.game.model.*;
import java.util.Objects;

public record LootRequest(UONpc victim, UOObject<?> killer, DeathCause cause, UOCorpse corpse) {
    public LootRequest {
        Objects.requireNonNull(victim, "Loot victim is required");
        Objects.requireNonNull(corpse, "Loot corpse is required");
        Objects.requireNonNull(cause, "Death cause is required");
    }
}
