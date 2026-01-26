package com.github.mayconr.juoserver.shard.commands;

import com.github.mayconr.juoserver.common.event.HandlerResult;
import com.github.mayconr.juoserver.common.event.Prompt;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Save extends AbstractCommand {
    private final RealmStorage realmStorage;

    public Save(RealmStorage realmStorage) {
        super("save");
        this.realmStorage = realmStorage;
    }

    @Override
    public HandlerResult handle(Prompt event) {
        realmStorage.saveMobiles()
                .thenAccept(mobiles->{
                    realmStorage.saveMobileRuntime();
                    realmStorage.saveMobileAttributes();
                    realmStorage.saveMobileVitals();
                }).whenComplete((unused, throwable) -> {
                    if (throwable!= null) {
                        log.error("unable to dele", throwable);
                    }
                });

        realmStorage.saveItems()
                .thenAccept(items->{
                    realmStorage.saveItemStates();
                }).whenComplete((unused, throwable) -> {
                    if (throwable != null) {
                        log.info("erro ",throwable);
                    }
                });

        return HandlerResult.CONTINUE;
    }
}
