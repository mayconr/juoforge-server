package com.github.mayconr.juoserver.network.session.i18n;

import java.util.Locale;

public enum ClientLocale {
    EN_US("en_US", Locale.US),
    PT_BR("pt_BR", Locale.of("pt", "BR")),
    ES_ES("es_ES", Locale.of("es", "ES"));

    private final String code;
    private final Locale javaLocale;

    ClientLocale(String code, Locale javaLocale) {
        this.code = code;
        this.javaLocale = javaLocale;
    }

    public String code() {
        return code;
    }

    public Locale toJavaLocale() {
        return javaLocale;
    }

    public static ClientLocale defaultLocale() {
        return EN_US;
    }

    public static ClientLocale fromCode(String code) {
        if (code == null || code.isBlank()) {
            return defaultLocale();
        }

        return switch (code.toLowerCase()) {
            case "en", "enu", "en_us" -> EN_US;
            case "pt", "ptb", "pt_br" -> PT_BR;
            case "es", "esp", "es_es" -> ES_ES;
            default -> defaultLocale();
        };
    }
}
