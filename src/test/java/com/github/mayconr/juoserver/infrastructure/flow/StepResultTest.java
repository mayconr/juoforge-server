package com.github.mayconr.juoserver.infrastructure.flow;

import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;

class StepResultTest {
    @Test
    void failurePreservesTechnicalReasonAndSeparateDisplayMessage() {
        var result = StepResult.failure("MOUNT_OUT_OF_RANGE", "Distance exceeds 2", "{mount.out_of_range}");

        assertTrue(result.flowFailed());
        assertFalse(result.shouldContinue());
        assertEquals("MOUNT_OUT_OF_RANGE", result.code());
        assertEquals("Distance exceeds 2", result.reason());
        assertEquals("{mount.out_of_range}", result.displayMessage());
    }

    @Test
    void existingFactoriesAndConstructorDoNotExposeTechnicalReasonAsDisplayMessage() {
        assertNull(StepResult.failure("Technical error").displayMessage());
        assertNull(StepResult.failure("CODE", "Technical error").displayMessage());
        assertNull(new StepResult(ResultStatus.FAILURE, null, "CODE", "Technical error").displayMessage());
        assertNull(StepResult.success().displayMessage());
    }

    @Test
    void mountDisplayKeysHaveDefaultAndPortugueseTranslations() {
        var defaults = ResourceBundle.getBundle("messages", Locale.ROOT);
        var portuguese = ResourceBundle.getBundle("messages", Locale.forLanguageTag("pt-BR"));
        for (var key : new String[]{"mount.invalid_target", "mount.already_mounted", "mount.out_of_range",
                "mount.not_mountable", "mount.equip_failed", "mount.failed"}) {
            assertFalse(defaults.getString(key).isBlank());
            assertFalse(portuguese.getString(key).isBlank());
            assertNotEquals(defaults.getString(key), portuguese.getString(key));
        }
    }
}
