package com.github.mayconr.juoserver.game.model.event;

import com.github.mayconr.juoserver.game.event.GameEvent;
import com.github.mayconr.juoserver.game.model.UOPlayer;

public record MobileSpeech(UOPlayer player, String message, SpeechContext context) implements GameEvent {


}
