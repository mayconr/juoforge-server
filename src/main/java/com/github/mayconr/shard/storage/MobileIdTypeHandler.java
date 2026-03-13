package com.github.mayconr.shard.storage;

import com.github.mayconr.juoserver.game.model.UOMobile;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MobileIdTypeHandler extends BaseTypeHandler<UOMobile> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, UOMobile parameter, JdbcType jdbcType) throws SQLException {
        ps.setObject(i, parameter.getId());
    }

    @Override
    public UOMobile getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return null;
    }

    @Override
    public UOMobile getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public UOMobile getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return null;
    }
}
