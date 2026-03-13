package com.github.mayconr.shard;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mayconr.juoserver.*;
import com.github.mayconr.juoserver.game.world.World;
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
            //cfg.getTypeHandlerRegistry().register("com.github.mayconr.world.storage.types");

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
    public World world(Executor databaseExecutor, SqlSessionFactory sessionFactory) {
        var worldConfig = WorldConfiguration.builder()
                .storage(s-> s
                        .mobile(new PsqlMobileStorage(databaseExecutor, sessionFactory))
                        .item(new PsqlItemStorage(databaseExecutor, sessionFactory))
                        .account(new PsqlAccountStorage(sessionFactory, databaseExecutor)))
                .build();
        var config = new JuoforgeConfiguration(EngineSettings.defaults(), worldConfig);
        var world = new WorldBootstrap(config).start();
        var network = new NetworkBootstrap(world).build();
        network.bind(9000);
        return world;
    }

    @Bean
    public ApplicationRunner configure(World world) {
        return args -> {
            world.on(new Goto(world));
            //world.on(new Save(worldStorage));
            world.on(new CreateNpc(world));
            world.on(new CreateItem(world));
            world.on(new TeleTo(world));
            world.on(new Kill(world, world));
            world.on(new Destroy(world));
            world.on(new Test(world, world));
            world.on(new CreateEquippedItem(world));
            world.on(new CreateContainerItem(world));
            world.on(new Mount(world, world));
            world.on(new Unmount(world));
            world.on(new CreateStack(world));
            world.on(new Region(world));
            world.on(new Where(world));
        };
    }
}
