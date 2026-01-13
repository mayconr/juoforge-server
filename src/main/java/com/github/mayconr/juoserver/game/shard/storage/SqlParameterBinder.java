package com.github.mayconr.juoserver.game.shard.storage;

import java.sql.PreparedStatement;

public interface SqlParameterBinder {
    void bind(PreparedStatement ps) throws Exception;
}
