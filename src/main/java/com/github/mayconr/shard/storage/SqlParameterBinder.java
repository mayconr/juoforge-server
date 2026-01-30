package com.github.mayconr.shard.storage;

import java.sql.PreparedStatement;

public interface SqlParameterBinder {
    void bind(PreparedStatement ps) throws Exception;
}
