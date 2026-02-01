package com.github.mayconr.shard;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mayconr.juoserver.common.event.EventBus;
import com.github.mayconr.juoserver.game.gump.GumpSystem;
import com.github.mayconr.juoserver.game.session.world.WorldInternal;
import com.github.mayconr.juoserver.infrastructure.server.ServerStartup;
import com.github.mayconr.juoserver.infrastructure.storage.AccountStorage;
import com.github.mayconr.juoserver.infrastructure.storage.ItemStorage;
import com.github.mayconr.juoserver.infrastructure.storage.MobileStorage;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import com.github.mayconr.shard.command.*;
import com.github.mayconr.shard.command.Test;
import com.github.mayconr.shard.storage.PsqlAccountStorage;
import com.github.mayconr.shard.storage.PsqlItemStorage;
import com.github.mayconr.shard.storage.PsqlMobileStorage;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
public class ShardConfiguration {

    @Bean
    public ServerStartup teste(ServerStartup startup) throws InterruptedException {
        startup.initialize();
        return startup;
    }

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
            EventBus bus, GumpSystem gumpSystem, WorldInternal worldInternal, RealmStorage worldStorage) {
        return args -> {
            bus.register(new Goto(gumpSystem, worldInternal));
            bus.register(new Save(worldStorage));
            bus.register(new CreateNpc(worldInternal));
            bus.register(new CreateItem(worldInternal));
            bus.register(new TeleTo(worldInternal));
            bus.register(new Kill(worldInternal, worldInternal));
            bus.register(new Destroy(worldInternal));
            bus.register(new Test(worldInternal, worldInternal));
        };
    }
}
