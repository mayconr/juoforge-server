package com.github.mayconr.juoserver.game.model.event;

import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.PointInTheWorld;
import com.github.mayconr.juoserver.game.model.TextType;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.network.packet.UnicodeSpeachRequest;

public record SpeechContext(
        UOMobile speaker,
        TextType type,
        SpeechRange range,
        int hue,
        int font,
        long timestamp,
        Location location
) {

    public static SpeechContext of(UOMobile speaker, UnicodeSpeachRequest request) {
        return new SpeechContext(
                speaker,
                request.getType(),
                SpeechRange.NORMAL,
                request.getHue(),
                request.getFont(),
                System.currentTimeMillis(),
                new PointInTheWorld(speaker)
        );
    }

}
