package com.github.mayconr.shard.storage;

import com.github.mayconr.juoserver.game.model.UOAccount;
import com.github.mayconr.shard.storage.types.UUIDTypeHandler;
import org.apache.ibatis.annotations.Arg;
import org.apache.ibatis.annotations.ConstructorArgs;
import org.apache.ibatis.annotations.Select;

import java.util.UUID;

public interface AccountMapper {

    @Select("SELECT id, username, password FROM accounts WHERE username = #{username}")
    @ConstructorArgs({
            @Arg(column = "id", javaType = UUID.class, typeHandler = UUIDTypeHandler.class),
            @Arg(column = "username", javaType = String.class),
            @Arg(column = "password", javaType = String.class),
    })
    UOAccount findByUsername(String username);

}
