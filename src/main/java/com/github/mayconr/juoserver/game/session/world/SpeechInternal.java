package com.github.mayconr.juoserver.game.session.world;

import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.network.packet.UnicodeSpeachRequest;

public interface SpeechInternal {

    void speech(UOPlayer player, UnicodeSpeachRequest request);

}
