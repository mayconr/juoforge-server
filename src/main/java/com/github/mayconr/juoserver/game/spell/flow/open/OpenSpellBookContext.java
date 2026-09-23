package com.github.mayconr.juoserver.game.spell.flow.open;

import com.github.mayconr.juoserver.game.model.SpellbookType;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractSyncFlowContext;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class OpenSpellBookContext extends AbstractSyncFlowContext<Void> {
    private final UOPlayer player;
    private final UOItem book;
    private final SpellbookType type;
    private final long spellMask;
}
