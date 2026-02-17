package com.github.mayconr.juoserver.game.interaction.action;

import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.game.model.ActionSubCommand;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.event.GuildButtonPressed;
import com.github.mayconr.juoserver.game.model.event.HelpButtonPressed;
import com.github.mayconr.juoserver.game.model.event.QuestButtonPressed;
import com.github.mayconr.juoserver.network.packet.ActionRequest;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class ActionHandler {

    private final EventBus eventBus;

    public void handleAction(UOPlayer player, ActionRequest request){
        if (request == null) {
            return;
        }

        final var command = Optional.ofNullable(request.getSubCommand()).orElse(ActionSubCommand.UNKNOWN);
        switch (command) {
            case QUEST_BUTTON -> eventBus.publish(new QuestButtonPressed(player));
            case GUILD_BUTTON -> eventBus.publish(new GuildButtonPressed(player));
            case HELP_BUTTON -> eventBus.publish(new HelpButtonPressed(player));
        }
    }

}
