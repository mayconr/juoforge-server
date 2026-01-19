package com.github.mayconr.juoserver.common.event;

import java.util.ArrayList;

import com.github.mayconr.juoserver.game.model.UOMobile;

public record Prompt(UOMobile mobile, String name, String[] arguments) implements GameEvent {

    public static Prompt newInstance(UOMobile mobile, String prompt) {
        final ArrayList<String> result = new ArrayList<>();
        int start = 0;
        for (int i = 0; i < prompt.length(); i++) {
            if (prompt.charAt(i) == ' ') {
                result.add(prompt.substring(start, i));
                start = i + 1;
            }
        }
        result.add(prompt.substring(start));

        final var commandName = result.get(0).substring(1);
        result.remove(0);

        return new Prompt(mobile, commandName, result.toArray(new String[] {}));
    }
}
