package com.github.mayconr.juoserver.game.world;

import com.github.mayconr.juoserver.game.economy.EconomyModule;

import com.github.mayconr.juoserver.game.ui.UICommands;
import com.github.mayconr.juoserver.game.player.PlayerCommands;
import com.github.mayconr.juoserver.game.mobile.MobileModule;
import com.github.mayconr.juoserver.game.item.ItemModule;
import com.github.mayconr.juoserver.game.skill.SkillModule;
import com.github.mayconr.juoserver.game.interaction.InteractionModule;

/** Engine access to module commands and operations without a module equivalent. */
public interface WorldInternal extends WorldModules {

    @Override
    UICommands ui();

    @Override
    PlayerCommands player();

    @Override
    MobileModule mobile();

    @Override
    ItemModule item();

    @Override
    SkillModule skill();

    @Override
    InteractionModule interaction();

    void initialize();

    @Override
    EconomyModule economy();

}
