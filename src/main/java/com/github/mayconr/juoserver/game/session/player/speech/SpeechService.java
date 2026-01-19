package com.github.mayconr.juoserver.game.session.player.speech;

import com.github.mayconr.juoserver.common.event.EventBus;
import com.github.mayconr.juoserver.common.event.MobileSpoke;
import com.github.mayconr.juoserver.common.event.Prompt;
import com.github.mayconr.juoserver.common.event.SpeechContext;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.session.SessionFanout;
import com.github.mayconr.juoserver.network.packet.SendSpeech;
import com.github.mayconr.juoserver.network.packet.UnicodeSpeachRequest;
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
