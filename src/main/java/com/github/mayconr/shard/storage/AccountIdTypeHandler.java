package com.github.mayconr.shard.storage;

import com.github.mayconr.juoserver.game.model.UOAccount;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AccountIdTypeHandler extends BaseTypeHandler<UOAccount> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, UOAccount parameter, JdbcType jdbcType) throws SQLException {
        ps.setObject(i, parameter.getId());
    }

    @Override
    public UOAccount getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return null;
    }

    @Override
    public UOAccount getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public UOAccount getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return null;
    }
}
