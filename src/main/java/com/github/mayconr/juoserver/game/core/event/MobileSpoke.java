package com.github.mayconr.juoserver.game.core.event;

import com.github.mayconr.juoserver.game.core.model.UOMobile;

public record MobileSpoke(UOMobile mobile, String message, SpeechContext context) implements GameEvent {


}
