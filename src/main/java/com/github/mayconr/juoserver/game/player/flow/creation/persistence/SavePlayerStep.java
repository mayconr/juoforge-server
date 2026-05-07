package com.github.mayconr.juoserver.game.player.flow.creation.persistence;

import com.github.mayconr.juoserver.game.player.flow.creation.PlayerCreationContext;
import com.github.mayconr.juoserver.game.world.SerialGenerator;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SavePlayerStep extends AbstractFlowStep<PlayerCreationContext> {

    private final RealmStorage storage;
    private final SerialGenerator serialGenerator;

    public SavePlayerStep(RealmStorage storage, SerialGenerator serialGenerator) {
        super("PersistPlayerStep");
        this.storage = storage;
        this.serialGenerator = serialGenerator;
    }

    @Override
    public StepResult execute(PlayerCreationContext context) {
        final int currentMobileSerialId = serialGenerator.getCurrentMobile();
        final int currentItemSerialId = serialGenerator.getCurrentItem();
        final var player = context.getPlayer();
        final var starterItems = context.getStarterItems();

        var future = storage.saveNewPlayerMobile(currentMobileSerialId, player, currentItemSerialId, starterItems)
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
