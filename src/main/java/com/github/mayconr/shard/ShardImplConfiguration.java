package com.github.mayconr.shard;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mayconr.juoserver.NetworkBootstrap;
import com.github.mayconr.juoserver.ServerRuntime;
import com.github.mayconr.juoserver.WorldBootstrap;
import com.github.mayconr.juoserver.infrastructure.template.TemplateRegistry;
import com.github.mayconr.shard.command.*;
import com.github.mayconr.shard.skills.crafting.mining.*;
import com.github.mayconr.shard.storage.PsqlAccountStorage;
import com.github.mayconr.shard.storage.PsqlItemStorage;
import com.github.mayconr.shard.storage.PsqlMobileStorage;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
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


            return factory;
        }
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public ServerRuntime runtime(Executor databaseExecutor, SqlSessionFactory sessionFactory) {

        var serverRuntime = new WorldBootstrap(cfg -> {
            cfg.mobileStorage(new PsqlMobileStorage(databaseExecutor, sessionFactory));
            cfg.itemStorage(new PsqlItemStorage(databaseExecutor, sessionFactory));
            cfg.accountStorage(new PsqlAccountStorage(sessionFactory, databaseExecutor));

            // Item trigger
            cfg.addItemTrigger(runtime->{
                final TemplateRegistry<String, Ore> oreRegistry = runtime.getTemplateRegistry("oreByName", Ore.class);
                final var oreResourceRoller = new OreResourceRoller(runtime.world(), oreRegistry);
                final var service = new MiningUseService(oreResourceRoller, runtime, new MiningTargetValidator());
                return new MiningToolTrigger(service);
            });

            // Listeners
            cfg.addEventListener(Goto::new);
            cfg.addEventListener(runtime->new CreateNpc(runtime.world()));
            cfg.addEventListener(runtime->new CreateStack(runtime.world()));
            cfg.addEventListener(runtime->new CreateItem(runtime.world()));
            cfg.addEventListener(runtime->new TeleTo(runtime.world()));
            cfg.addEventListener(runtime->new Kill(runtime.world()));
            cfg.addEventListener(runtime->new Destroy(runtime.world()));
            cfg.addEventListener(runtime->new CreateEquippedItem(runtime.world()));
            cfg.addEventListener(runtime -> new Info(runtime.world()));
            cfg.addEventListener(Mount::new);
            cfg.addEventListener(Unmount::new);
            cfg.addEventListener(Region::new);
            cfg.addEventListener(Where::new);
            cfg.addEventListener(Bounds::new);
            cfg.addEventListener(ApplyDamage::new);
            cfg.addEventListener(Resurrect::new);
            cfg.addEventListener(r->new Save(r.storage()));
            cfg.addCustomTemplate("oreByName", Ore.class, Ore::name, Path.of("template/skills/mining"));

        }).start();

        var network = new NetworkBootstrap(serverRuntime).build();
        network.bindAsync(9000);
        return serverRuntime;
    }

}
