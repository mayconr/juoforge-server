package com.github.mayconr.juoserver.game.shard.storage;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface RowMapper<T> {
    T map(ResultSet rs) throws SQLException;
}
