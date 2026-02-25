package com.github.mayconr.shard;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mayconr.juoserver.game.item.template.ItemTemplateRegistry;
import com.github.mayconr.juoserver.game.world.World;
import com.github.mayconr.juoserver.game.world.bootstrap.ShardConfiguration;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.server.ServerStartup;
import com.github.mayconr.juoserver.infrastructure.storage.AccountStorage;
import com.github.mayconr.juoserver.infrastructure.storage.ItemStorage;
import com.github.mayconr.juoserver.infrastructure.storage.MobileStorage;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import com.github.mayconr.shard.command.*;
import com.github.mayconr.shard.command.Test;
import com.github.mayconr.shard.storage.*;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.Reader;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
public class ShardImplConfiguration {

    @Bean
    public ShardConfiguration getShardConfiguration(ItemTemplateRegistry itemTemplateRegistry) {
        return ShardConfiguration.builder()
                .build();
    }

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
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws IOException {
        try (Reader reader = Resources.getResourceAsReader("mybatis-config.xml")) {
            var factoryBuilder = new SqlSessionFactoryBuilder();
            SqlSessionFactory factory = factoryBuilder.build(reader);

            // 2) Usa a MESMA Configuration que veio do XML
            var cfg = factory.getConfiguration();

            // 3) Força o Environment com o DataSource do Spring
            var env = new Environment("dev", new JdbcTransactionFactory(), dataSource);
            cfg.setEnvironment(env);

            // 4) (Opcional) registra package de TypeHandlers via código
            // Se você já colocou <typeHandlers><package .../> no mybatis-config.xml, pode remover isso.
            //cfg.getTypeHandlerRegistry().register("com.github.mayconr.shard.storage.types");

            // 5) Registra as interfaces @Mapper (o XML já estará carregado via <mappers>)
            //cfg.addMapper(MobileMapper.class);
            cfg.addMapper(AccountMapper.class);
            cfg.addMapper(ItemMapper.class);

            return factory;
        }
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public AccountStorage accountStorage(SqlSessionFactory sessionFactory, Executor databaseExecutor) {
        return new PsqlAccountStorage(sessionFactory, databaseExecutor);
    }

    @Bean
    public MobileStorage mobileStorage(Executor databaseExecutor, SqlSessionFactory sessionFactory) {
        return new PsqlMobileStorage(databaseExecutor, sessionFactory);
    }

    @Bean
    public ItemStorage itemStorage(Executor databaseExecutor, SqlSessionFactory sessionFactory) {
        return new PsqlItemStorage(databaseExecutor, sessionFactory);
    }

    @Bean
    public ApplicationRunner configure(
            EventBus bus, World world, RealmStorage worldStorage) {
        return args -> {
            bus.register(new Goto(world));
            bus.register(new Save(worldStorage));
            bus.register(new CreateNpc(world));
            bus.register(new CreateItem(world));
            bus.register(new TeleTo(world));
            bus.register(new Kill(world, world));
            bus.register(new Destroy(world));
            bus.register(new Test(world, world));
            bus.register(new CreateEquippedItem(world));
            bus.register(new CreateContainerItem(world));
            bus.register(new Mount(world, world));
            bus.register(new Unmount(world));
            bus.register(new CreateStack(world));
            bus.register(new Region(world));
        };
    }
}
