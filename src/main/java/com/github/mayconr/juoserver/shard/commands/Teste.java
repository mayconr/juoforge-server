package com.github.mayconr.juoserver.shard.commands;

import com.github.mayconr.juoserver.common.event.HandlerResult;
import com.github.mayconr.juoserver.common.event.Prompt;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;

public class Teste extends AbstractCommand {

    private RealmStorage storage;

    public Teste(RealmStorage storage) {
        super("maycon");
        this.storage = storage;
    }

    @Override
    public HandlerResult handle(Prompt event) {
        event.mobile().addAttribute(event.arguments()[0], event.arguments()[1]);
        storage.saveMobileRuntime()
                .whenComplete(((uoMobiles, throwable) -> {
                    if (throwable!=null) {
                        throwable.printStackTrace();
                    }
                }));
        return HandlerResult.CONTINUE;
    }
}
