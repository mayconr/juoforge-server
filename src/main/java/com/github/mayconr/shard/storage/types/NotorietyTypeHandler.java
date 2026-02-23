package com.github.mayconr.shard.storage.types;

import com.github.mayconr.juoserver.game.model.Notoriety;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class NotorietyTypeHandler extends BaseTypeHandler<Notoriety> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Notoriety parameter, JdbcType jdbcType) throws SQLException {
        ps.setInt(i, parameter.getCode());
    }

    @Override
    public Notoriety getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return Notoriety.fromCode(rs.getInt(columnName));
    }

    @Override
    public Notoriety getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return Notoriety.fromCode(rs.getInt(columnIndex));
    }

    @Override
    public Notoriety getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return Notoriety.fromCode(cs.getInt(columnIndex));
    }
}
