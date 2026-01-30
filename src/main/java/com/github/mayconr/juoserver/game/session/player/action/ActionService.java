package com.github.mayconr.juoserver.game.session.player.action;

import com.github.mayconr.juoserver.common.event.EventBus;
import com.github.mayconr.juoserver.common.event.GuildButtonPressed;
import com.github.mayconr.juoserver.common.event.HelpButtonPressed;
import com.github.mayconr.juoserver.common.event.QuestButtonPressed;
import com.github.mayconr.juoserver.game.model.ActionSubCommand;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.network.packet.ActionRequest;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class ActionService {

    private final UOPlayer player;
    private final EventBus eventBus;

    public void handleAction(ActionRequest request) {
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
