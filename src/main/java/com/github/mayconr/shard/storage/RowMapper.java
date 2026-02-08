package com.github.mayconr.shard.storage;

import java.sql.ResultSet;

public interface RowMapper<T> {
    T map(ResultSet rs) throws Exception;
}
