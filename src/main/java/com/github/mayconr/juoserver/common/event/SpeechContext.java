package com.github.mayconr.juoserver.common.event;

import com.github.mayconr.juoserver.game.model.UOMobile;

public record SpeechContext(
        UOMobile speaker,
        SpeechType type,
        SpeechRange range,
        long timestamp
) {

    public static SpeechContext of(UOMobile speaker) {
        return new SpeechContext(
                speaker,
                SpeechType.SAY,
                SpeechRange.NORMAL,
                System.currentTimeMillis()
        );
    }

    public SpeechContext withType(SpeechType type) {
        return new SpeechContext(
                speaker, type, range, timestamp
        );
    }

    public SpeechContext withRange(SpeechRange range) {
        return new SpeechContext(
                speaker, type, range, timestamp
        );
    }

}
