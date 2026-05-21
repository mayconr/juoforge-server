package com.github.mayconr.juoserver.game.mobile.flow.death;

import com.github.mayconr.juoserver.game.model.DeathCause;
import com.github.mayconr.juoserver.game.model.UOCorpse;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UOObject;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractSyncFlowContext;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@RequiredArgsConstructor
public class DeathContext extends AbstractSyncFlowContext<Void> {

    private final UOMobile victim;
    private final UOObject<?> killer;
    private final DeathCause cause;
    private UOCorpse corpse;

    public void corpse(UOCorpse corpse) {
        this.corpse = corpse;
    }
}
