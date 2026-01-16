package com.github.mayconr.juoserver.game.core.session.player.speech;

import com.github.mayconr.juoserver.game.core.event.EventBus;
import com.github.mayconr.juoserver.game.core.event.MobileSpoke;
import com.github.mayconr.juoserver.game.core.event.Prompt;
import com.github.mayconr.juoserver.game.core.event.SpeechContext;
import com.github.mayconr.juoserver.game.core.model.UOMobile;
import com.github.mayconr.juoserver.game.core.session.SessionFanout;
import com.github.mayconr.juoserver.game.packet.SendSpeech;
import com.github.mayconr.juoserver.game.packet.UnicodeSpeachRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class SpeechService {

    private static final int MAX_SPEECH_LENGTH = 200;
    private final UOMobile mobile;
    private final EventBus eventBus;
    private final SessionFanout fanout;
    private final SpeechSanitizer sanitizer = new DefaultSpeechSanitizer();
    private final SpeechRateLimiter rateLimiter = new FixedWindowSpeechRateLimiter();

    public void handleSpeech(UnicodeSpeachRequest request) {
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

        if (!rateLimiter.allow(mobile)) {
            log.info("Speech rate limit reached!");
            return;
        }

        if (isCommand(text)) {
            handleCommand(text);
        } else {
            handleSpeechSay(text, request);
        }
    }

    private boolean isCommand(String text) {
        return text.startsWith(".");
    }

    private void handleCommand(String text) {
        // TODO check if mobile has permission

        eventBus.publish(Prompt.newInstance(mobile, text));
    }

    private void handleSpeechSay(String text, UnicodeSpeachRequest request) {
        fanout.writeAndFlush(new SendSpeech(mobile, request));
        eventBus.publish(new MobileSpoke(mobile, text, SpeechContext.of(mobile)));
    }
}
