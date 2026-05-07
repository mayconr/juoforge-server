package com.github.mayconr.shard.storage.types;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mayconr.juoserver.game.model.BehaviorDefinition;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BehaviorDefinitionTypeHandler extends BaseTypeHandler<BehaviorDefinition> {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, BehaviorDefinition parameter, JdbcType jdbcType) throws SQLException {
        try {
            ps.setString(i, mapper.writeValueAsString(parameter));
        } catch (JsonProcessingException e) {
            throw new SQLException("Erro ao serializar BehaviorDefinition", e);
        }
    }

    @Override
    public BehaviorDefinition getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return parse(rs.getString(columnName));
    }

    @Override
    public BehaviorDefinition getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return parse(rs.getString(columnIndex));
    }

    @Override
    public BehaviorDefinition getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return parse(cs.getString(columnIndex));
    }

    private BehaviorDefinition parse(String json) throws SQLException {
        if (json == null) return null;
        try {
            return mapper.readValue(json, BehaviorDefinition.class);
        } catch (Exception e) {
            throw new SQLException("Erro ao deserializar BehaviorDefinition", e);
        }
    }
}
