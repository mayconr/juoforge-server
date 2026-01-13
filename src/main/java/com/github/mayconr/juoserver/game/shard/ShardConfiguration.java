package com.github.mayconr.juoserver.game.shard;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.sql.DataSource;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.mayconr.juoserver.game.core.event.EventBus;
import com.github.mayconr.juoserver.game.core.gump.GumpSystem;
import com.github.mayconr.juoserver.game.core.session.game.GameSession;
import com.github.mayconr.juoserver.game.shard.commands.Goto;
import com.github.mayconr.juoserver.game.shard.storage.PsqlAccountStorage;
import com.github.mayconr.juoserver.game.shard.storage.PsqlItemStorage;
import com.github.mayconr.juoserver.game.shard.storage.PsqlMobileStorage;
import com.github.mayconr.juoserver.game.storage.account.AccountStorage;
import com.github.mayconr.juoserver.game.storage.item.ItemStorage;
import com.github.mayconr.juoserver.game.storage.mobile.MobileStorage;
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
        config.setAutoCommit(false);

        return new HikariDataSource(config);
    }

    @Bean
    public AccountStorage accountStorage(DataSource dataSource, Executor databaseExecutor) {
        return new PsqlAccountStorage(dataSource, databaseExecutor);
    }

    @Bean
    public MobileStorage mobileStorage(DataSource dataSource, Executor databaseExecutor) {
        return new PsqlMobileStorage(dataSource, databaseExecutor);
    }

    @Bean
    public ItemStorage itemStorage(DataSource dataSource, Executor databaseExecutor) {
        return new PsqlItemStorage(dataSource, databaseExecutor, new ObjectMapper());
    }

    @Bean
    public ApplicationRunner configure(
            EventBus bus, GumpSystem gumpSystem, GameSession gameSession) {
        return args -> {
            bus.register(new Goto(gumpSystem, gameSession));
        };
    }
}
