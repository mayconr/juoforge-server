package com.github.mayconr.juoserver.game.messaging.template;

import com.github.mayconr.juoserver.game.model.TextType;
import com.github.mayconr.juoserver.infrastructure.template.BaseTemplate;

public record MessageStyleTemplate(String name, TextType type, int hue, int font) implements BaseTemplate {
}
