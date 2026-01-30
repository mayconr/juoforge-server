package com.github.mayconr.juoserver.game.model;

public record MessageOptions(TextType type, int hue, int font, UOObject object) {

    public static MessageOptions of(TextType type, int hue, int font) {
        return new MessageOptions(type, hue, font, null);
    }

    public static MessageOptions of(TextType type, int hue, int font, UOObject object) {
        return new MessageOptions(type, hue, font, object);
    }

    public static MessageOptions standard() {
        return new MessageOptions(TextType.NORMAL, 105, 0, null);
    }
}
