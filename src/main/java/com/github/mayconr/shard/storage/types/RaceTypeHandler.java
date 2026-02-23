package com.github.mayconr.shard.storage.types;

import com.github.mayconr.juoserver.game.model.Race;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RaceTypeHandler extends BaseTypeHandler<Race> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Race parameter, JdbcType jdbcType) throws SQLException {
        ps.setInt(i, parameter.getCode());
    }

    @Override
    public Race getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return Race.fromCode(rs.getInt(columnName));
    }

    @Override
    public Race getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return Race.fromCode(rs.getInt(columnIndex));
    }

    @Override
    public Race getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return Race.fromCode(cs.getInt(columnIndex));
    }
}
