package com.github.mayconr.shard.storage.types;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mayconr.juoserver.game.model.AttributeMap;
import com.github.mayconr.juoserver.game.model.DefaultAttributeMap;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class AttributesTypeHandler extends BaseTypeHandler<AttributeMap> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, AttributeMap parameter, JdbcType jdbcType) throws SQLException {
        try {
            ps.setString(i, objectMapper.writeValueAsString(parameter.toMap()));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public AttributeMap getNullableResult(ResultSet rs, String columnName) throws SQLException {
        try {
            final Map<String, Object> values = objectMapper.readValue(rs.getString(columnName), new TypeReference<>() {});
            return new DefaultAttributeMap(values);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public AttributeMap getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        try {
            final Map<String, Object> values = objectMapper.readValue(rs.getString(columnIndex), new TypeReference<>() {});
            return new DefaultAttributeMap(values);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public AttributeMap getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        try {
            final Map<String, Object> values = objectMapper.readValue(cs.getString(columnIndex), new TypeReference<>() {});
            return new DefaultAttributeMap(values);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
