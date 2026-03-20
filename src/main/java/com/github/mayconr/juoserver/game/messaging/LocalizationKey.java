package com.github.mayconr.juoserver.game.messaging;

public class LocalizationKey {

    private LocalizationKey() {}

    public static boolean isLocalizationKey(String value){
        if(value == null || value.length() < 3){
            return false;
        }

        return value.startsWith("{") && value.endsWith("}");
    }

    public static String extractKey(String value){
        if(!isLocalizationKey(value)){
            return value;
        }

        return value.substring(1, value.length() - 1);
    }

}
