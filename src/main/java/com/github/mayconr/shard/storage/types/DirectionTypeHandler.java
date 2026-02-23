package com.github.mayconr.shard.storage.types;

import com.github.mayconr.juoserver.game.model.Direction;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DirectionTypeHandler extends BaseTypeHandler<Direction> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Direction parameter, JdbcType jdbcType) throws SQLException {
        ps.setInt(i, parameter.getCode());
    }

    @Override
    public Direction getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return Direction.fromCode(rs.getInt(columnName));
    }

    @Override
    public Direction getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return Direction.fromCode(rs.getInt(columnIndex));
    }

    @Override
    public Direction getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return Direction.fromCode(cs.getInt(columnIndex));
    }
}
