package com.github.mayconr.juoserver.game.player.flow.creation;

import com.github.mayconr.juoserver.game.flow.PlayerCreationFlowDefinition.PlayerCreationContext;
import com.github.mayconr.juoserver.game.world.SerialGenerator;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PersistPlayerStep extends AbstractFlowStep<PlayerCreationContext> {

    private final RealmStorage storage;
    private final SerialGenerator serialGenerator;

    public PersistPlayerStep(RealmStorage storage, SerialGenerator serialGenerator) {
        super("PersistPlayerStep");
        this.storage = storage;
        this.serialGenerator = serialGenerator;
    }

    @Override
    public StepResult execute(PlayerCreationContext context) {
        final int mobileSerialId = serialGenerator.getCurrentMobile();
        final int itemSerialId = serialGenerator.getCurrentItem();
        final var player = context.getPlayer();

        var future = storage.insertPlayerMobile(mobileSerialId, itemSerialId, player)
                .thenApply(persisted->{
                    context.complete(persisted);
                    return StepResult.success();
                }).whenComplete((persisted, throwable) -> {
                    if (throwable != null) {
                        log.error("PersistPlayerStep exception", throwable);
                        context.fail(throwable);
                    }
                });

        return StepResult.async(future);
    }
}
