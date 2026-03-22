package com.github.mayconr.juoserver.game.interaction.speech;

import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.event.MobileSpeech;
import com.github.mayconr.juoserver.game.model.event.Prompt;
import com.github.mayconr.juoserver.game.model.event.SpeechContext;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.network.packet.UnicodeSpeachRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class SpeechHandler {

    private static final int MAX_SPEECH_LENGTH = 200;
    private final SpeechSanitizer sanitizer = new DefaultSpeechSanitizer();
    private final SpeechRateLimiter rateLimiter = new FixedWindowSpeechRateLimiter();
    private final EventBus eventBus;

    public void speech(UOPlayer player, UnicodeSpeachRequest request) {
        if (request == null) {
            return;
        }

        final var rawText = request.getText();
        if (rawText == null) {
            return;
        }

        var text = sanitizer.normalize(rawText);

        if (text.isBlank() || text.length() > MAX_SPEECH_LENGTH) {
            return;
        }

        if (!rateLimiter.allow(player)) {
            log.info("Speech rate limit reached!");
            return;
        }

        if (isCommand(text)) {
            handleCommand(player, text);
        } else {
            handleSpeechSay(player, text, request);
        }

    }

    private boolean isCommand(String text) {
        return text.startsWith(".");
    }

    private void handleCommand(UOPlayer player, String text) {
        // TODO check if player has permission

        eventBus.publish(Prompt.newInstance(player, text));
    }

    private void handleSpeechSay(UOPlayer player, String text, UnicodeSpeachRequest request) {
        eventBus.publish(new MobileSpeech(player, text, SpeechContext.of(player, request)));
    }
}
