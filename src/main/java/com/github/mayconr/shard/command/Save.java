package com.github.mayconr.shard.command;

import com.github.mayconr.juoserver.game.model.event.Prompt;
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
    public void handle(Prompt event) {
        realmStorage.saveMobiles()
                .thenAccept(mobiles->{
                    realmStorage.saveMobileRuntime();
                    realmStorage.saveMobileAttributes();
                    realmStorage.saveMobileVitals();
                }).whenComplete((unused, throwable) -> {
                    if (throwable!= null) {
                        log.error("unable to dele", throwable);
                        return;
                    }

                    realmStorage.saveItems()
                            .thenAccept(items->{
                                realmStorage.saveItemStates();
                            }).whenComplete((unused2, throwabl2) -> {
                                if (throwabl2 != null) {
                                    log.info("erro ",throwable);
                                }
                            });
                });


    }
}
