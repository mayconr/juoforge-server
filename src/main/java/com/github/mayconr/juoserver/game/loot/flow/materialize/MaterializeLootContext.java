package com.github.mayconr.juoserver.game.loot.flow.materialize;

import com.github.mayconr.juoserver.game.model.UOCorpse;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractSyncFlowContext;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.util.List;

@Getter
@RequiredArgsConstructor
public final class MaterializeLootContext extends AbstractSyncFlowContext<List<UOItem>> {
    private final UOCorpse corpse;
}
