package com.github.mayconr.juoserver.game.combat.flow.warmode;

import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.WarModeType;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractContext;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ToggleWarModeContext extends AbstractContext {
    private final UOPlayer player;
    private final WarModeType type;
}
