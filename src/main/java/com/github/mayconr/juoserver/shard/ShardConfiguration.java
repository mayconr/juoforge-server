package com.github.mayconr.juoserver.shard;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.sql.DataSource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mayconr.juoserver.shard.commands.*;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.mayconr.juoserver.common.event.EventBus;
import com.github.mayconr.juoserver.game.gump.GumpSystem;
import com.github.mayconr.juoserver.game.session.world.WorldSession;
import com.github.mayconr.juoserver.shard.storage.PsqlAccountStorage;
import com.github.mayconr.juoserver.shard.storage.PsqlItemStorage;
import com.github.mayconr.juoserver.shard.storage.PsqlMobileStorage;
import com.github.mayconr.juoserver.infrastructure.storage.AccountStorage;
import com.github.mayconr.juoserver.infrastructure.storage.ItemStorage;
import com.github.mayconr.juoserver.infrastructure.storage.MobileStorage;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
public class ShardConfiguration {

    // Database
    @Bean
    public Executor databaseExecutor() {
        return Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors() / 2,
                r -> {
                    Thread t = new Thread(r);
                    t.setName("db-worker");
                    t.setDaemon(true);
                    return t;
                });
    }

    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:postgresql://localhost:5432/juoforge");
        config.setUsername("postgres");
        config.setPassword("postgres");

        config.setMaximumPoolSize(20);
        config.setMinimumIdle(5);
        config.setAutoCommit(true);

        return new HikariDataSource(config);
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public AccountStorage accountStorage(DataSource dataSource, Executor databaseExecutor) {
        return new PsqlAccountStorage(dataSource, databaseExecutor);
    }

    @Bean
    public MobileStorage mobileStorage(DataSource dataSource, Executor databaseExecutor, ObjectMapper objectMapper) {
        return new PsqlMobileStorage(dataSource, databaseExecutor, objectMapper);
    }

    @Bean
    public ItemStorage itemStorage(DataSource dataSource, Executor databaseExecutor) {
        return new PsqlItemStorage(dataSource, databaseExecutor, new ObjectMapper());
    }

    @Bean
    public ApplicationRunner configure(
            EventBus bus, GumpSystem gumpSystem, WorldSession worldSession, RealmStorage worldStorage) {
        return args -> {
            bus.register(new Goto(gumpSystem, worldSession));
            bus.register(new Save(worldStorage));
            bus.register(new CreateNpc(worldSession));
            bus.register(new CreateItem(worldSession));
            bus.register(new Teste(worldStorage));
            bus.register(new TeleTo(worldSession));
            bus.register(new Kill(worldSession));
            bus.register(new Destroy(worldSession));
        };
    }
}
