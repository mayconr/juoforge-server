package com.github.mayconr.juoserver.shard.storage;

import java.sql.PreparedStatement;

public interface SqlParameterBinder {
    void bind(PreparedStatement ps) throws Exception;
}
