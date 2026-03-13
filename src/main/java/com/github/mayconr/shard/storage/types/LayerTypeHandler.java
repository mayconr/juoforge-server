package com.github.mayconr.shard.storage.types;

import com.github.mayconr.juoserver.game.model.Layer;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedJdbcTypes(JdbcType.INTEGER)
@MappedTypes(Layer.class)
public class LayerTypeHandler extends BaseTypeHandler<Layer> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Layer parameter, JdbcType jdbcType) throws SQLException {
        ps.setInt(i, parameter.getCode());
    }

    @Override
    public Layer getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return Layer.fromCode(rs.getInt(columnName));
    }

    @Override
    public Layer getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return Layer.fromCode(rs.getInt(columnIndex));
    }

    @Override
    public Layer getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return Layer.fromCode(cs.getInt(columnIndex));
    }
}
