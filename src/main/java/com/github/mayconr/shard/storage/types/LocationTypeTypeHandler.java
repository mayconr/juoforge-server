package com.github.mayconr.shard.storage.types;

import com.github.mayconr.juoserver.game.model.ItemLocationType;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LocationTypeTypeHandler extends BaseTypeHandler<ItemLocationType> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, ItemLocationType parameter, JdbcType jdbcType) throws SQLException {
        ps.setInt(i, parameter.getCode());
    }

    @Override
    public ItemLocationType getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return ItemLocationType.fromCode(rs.getInt(columnName));
    }

    @Override
    public ItemLocationType getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return ItemLocationType.fromCode(rs.getInt(columnIndex));
    }

    @Override
    public ItemLocationType getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return ItemLocationType.fromCode(cs.getInt(columnIndex));
    }
}
