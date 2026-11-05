package com.github.mayconr.juoserver.network.session.i18n;

import com.github.mayconr.juoserver.DefaultWorldConfigurator;
import com.github.mayconr.juoserver.game.model.event.message.MessageContent;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class ResourceBundleMessageLocalizerTest {
    private final MessageLocalizer localizer = new ResourceBundleMessageLocalizer(
            List.of("i18n.missing", "i18n.shard", "i18n.core"), ClientLocale.EN_US);

    private String text(String key) {
        return localizer.localize(MessageContent.localized(key), ClientLocale.PT_BR);
    }

    @Test
    void retainsDefaultLibraryBundleSupport() {
        var defaults = new ResourceBundleMessageLocalizer("messages");
        assertEquals("That is too far away", defaults.localize(
                MessageContent.localized("drop.too_far_away"), ClientLocale.EN_US));
    }

    @Test
    void prioritizesShardAndPreservesCoreTranslations() {
        assertEquals("Shard português", text("shared"));
        assertEquals("Tradução da biblioteca", text("translated"));
        assertEquals("Core default", text("coreOnly"));
        assertEquals("Shard default", text("rootOnly"));
        assertEquals("Português", text("languageOnly"));
    }

    @Test
    void supportsFallbackMissingKeysAndNamedParameters() {
        assertEquals("English fallback", text("fallbackOnly"));
        assertEquals("!missing!", text("missing"));
        assertEquals("Olá, Maria!", localizer.localize(
                MessageContent.localized("{welcome}", Map.of("name", "Maria")), ClientLocale.PT_BR));
        assertEquals("English fallback", localizer.localize(MessageContent.localized("fallbackOnly"), null));
    }

    @Test
    void doesNotUseJvmDefaultLocale() {
        var original = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("pt-BR"));
            assertEquals("Shard root", localizer.localize(MessageContent.localized("shared"), ClientLocale.ES_ES));
        } finally {
            Locale.setDefault(original);
        }
    }

    @Test
    void registersBundlesInOrderWithoutDuplicatesAndReturnsSnapshot() {
        var cfg = new DefaultWorldConfigurator();
        cfg.addMessageBundle("i18n.shard");
        var snapshot = cfg.messageBundles();
        cfg.addMessageBundle("i18n.core");
        cfg.addMessageBundle("i18n.shard");
        assertEquals(List.of("i18n.shard"), snapshot);
        assertEquals(List.of("i18n.shard", "i18n.core"), cfg.messageBundles());
        assertThrows(UnsupportedOperationException.class, () -> snapshot.add("other"));
        assertThrows(IllegalArgumentException.class, () -> cfg.addMessageBundle(" "));
        assertThrows(IllegalArgumentException.class, () -> new ResourceBundleMessageLocalizer(List.of(), null));
    }
}
