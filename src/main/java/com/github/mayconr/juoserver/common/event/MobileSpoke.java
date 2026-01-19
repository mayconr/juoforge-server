package com.github.mayconr.juoserver.common.event;

import com.github.mayconr.juoserver.game.model.UOMobile;

public record MobileSpoke(UOMobile mobile, String message, SpeechContext context) implements GameEvent {


}
