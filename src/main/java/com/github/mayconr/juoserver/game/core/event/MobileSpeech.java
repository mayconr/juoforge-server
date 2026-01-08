package com.github.mayconr.juoserver.game.core.event;

import com.github.mayconr.juoserver.game.core.model.UOMobile;

public record MobileSpeech(UOMobile mobile, String message) implements GameEvent {}
