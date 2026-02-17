package com.github.mayconr.juoserver.game.interaction.speech;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class DefaultSpeechSanitizer implements SpeechSanitizer {
    // Remove control chars except \n and \t
    private static final Pattern CONTROL_CHARS =
            Pattern.compile("[\\p{Cc}\\p{Cf}]");

    // Colapsa espaços múltiplos
    private static final Pattern MULTIPLE_SPACES =
            Pattern.compile("\\s{2,}");

    @Override
    public String normalize(String input) {
        if (input == null) {
            return "";
        }

        // 1. Unicode normalization (NFKC avoid visual spoofing)
        String text = Normalizer.normalize(input, Normalizer.Form.NFKC);

        // 2. Remove controls
        text = CONTROL_CHARS.matcher(text).replaceAll("");

        // 3. Remove whitespaces
        text = text.trim();

        // 4. Remove multples spaces
        text = MULTIPLE_SPACES.matcher(text).replaceAll(" ");

        return text;
    }
}
