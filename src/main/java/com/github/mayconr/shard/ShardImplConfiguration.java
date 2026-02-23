package com.github.mayconr.shard;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mayconr.juoserver.game.item.template.ItemTemplateRegistry;
import com.github.mayconr.juoserver.game.model.CharacterStatus;
import com.github.mayconr.juoserver.game.model.Direction;
import com.github.mayconr.juoserver.game.model.Notoriety;
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
import com.github.mayconr.shard.storage.MobileSqlMapper;
import com.github.mayconr.shard.storage.PsqlAccountStorage;
import com.github.mayconr.shard.storage.PsqlItemStorage;
import com.github.mayconr.shard.storage.PsqlMobileStorage;
import com.github.mayconr.shard.storage.types.*;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.UUID;
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
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) {
        var env = new Environment("dev", new JdbcTransactionFactory(),  dataSource);
        var config = new org.apache.ibatis.session.Configuration(env);
        config.addMapper(MobileSqlMapper.class);

        var types = config.getTypeHandlerRegistry();
        types.register(GenderTypeHandler.class);
        types.register(RaceTypeHandler.class);
        types.register(Notoriety.class, new NotorietyTypeHandler());
        types.register(CharacterStatus.class, new CharacterStatusTypeHandler());
        types.register(Direction.class, new DirectionTypeHandler());
        types.register(UUID.class, new UUIDTypeHandler());

        var builder = new SqlSessionFactoryBuilder();
        return builder.build(config);
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
    public MobileStorage mobileStorage(DataSource dataSource, Executor databaseExecutor, ObjectMapper objectMapper, SqlSessionFactory sessionFactory) {
        return new PsqlMobileStorage(dataSource, databaseExecutor, objectMapper, sessionFactory);
    }

    @Bean
    public ItemStorage itemStorage(DataSource dataSource, Executor databaseExecutor) {
        return new PsqlItemStorage(dataSource, databaseExecutor, new ObjectMapper());
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
        };
    }
}
