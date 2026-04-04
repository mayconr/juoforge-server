package com.github.mayconr.shard;

import com.github.mayconr.juoserver.ServerRuntime;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Teste implements ApplicationRunner {

    private final ServerRuntime runtime;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        var item = runtime.storage().loadItem(1073741824).join();
        System.out.println(item);
    }
}
