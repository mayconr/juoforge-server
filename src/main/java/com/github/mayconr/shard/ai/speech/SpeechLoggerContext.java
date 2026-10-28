package com.github.mayconr.shard.ai.speech;

import com.github.mayconr.juoserver.game.ai.definition.AIFlowContext;
import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.game.world.World;
import com.github.mayconr.juoserver.game.model.GameMath;
import com.github.mayconr.juoserver.game.model.event.MobileSpeech;
import java.util.Optional;
import java.util.function.Predicate;

public final class SpeechLoggerContext extends AIFlowContext {
    public static final String AI_TYPE = "SPEECH_LOGGER";
    public static final int SPEECH_RADIUS = 10;

    public SpeechLoggerContext(UONpc npc, World world) {
        super(npc, world);
    }

    @Override
    public Optional<Predicate<MobileSpeech>> speechFilter() {
        return Optional.of(speech -> speech.mobile() != null && speech.message() != null
                && GameMath.isInRange(npc(), speech.mobile(), SPEECH_RADIUS));
    }
}
