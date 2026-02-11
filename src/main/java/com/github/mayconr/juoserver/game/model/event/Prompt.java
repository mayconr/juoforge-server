package com.github.mayconr.juoserver.game.model.event;

import java.util.ArrayList;

import com.github.mayconr.juoserver.game.event.GameEvent;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UOPlayer;

public record Prompt(UOPlayer player, String name, String[] arguments) implements GameEvent {

    public static Prompt newInstance(UOPlayer player, String prompt) {
        final ArrayList<String> result = new ArrayList<>();
        int start = 0;
        for (int i = 0; i < prompt.length(); i++) {
            if (prompt.charAt(i) == ' ') {
                result.add(prompt.substring(start, i));
                start = i + 1;
            }
        }
        result.add(prompt.substring(start));

        final var commandName = result.getFirst().substring(1);
        result.removeFirst();

        return new Prompt(player, commandName, result.toArray(new String[] {}));
    }
}
