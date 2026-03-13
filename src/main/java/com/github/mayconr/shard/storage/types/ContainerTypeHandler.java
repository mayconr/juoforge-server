package com.github.mayconr.shard.storage.types;

import com.github.mayconr.juoserver.game.model.Container;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ContainerTypeHandler extends BaseTypeHandler<Container> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Container parameter, JdbcType jdbcType) throws SQLException {
        ps.setObject(i, parameter.getId());
    }

    @Override
    public Container getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return null;
    }

    @Override
    public Container getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public Container getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return null;
    }
}
