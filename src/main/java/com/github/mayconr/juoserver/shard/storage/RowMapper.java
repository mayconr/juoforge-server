package com.github.mayconr.juoserver.shard.storage;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface RowMapper<T> {
    T map(ResultSet rs) throws SQLException;
}
