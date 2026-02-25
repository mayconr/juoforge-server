package com.github.mayconr.shard.storage.types;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mayconr.juoserver.game.model.ItemFlag;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class ItemFlagTypeHandler extends BaseTypeHandler<List<ItemFlag>> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<ItemFlag> parameter, JdbcType jdbcType) throws SQLException {
        try {
            ps.setString(i, objectMapper.writeValueAsString(parameter));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<ItemFlag> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        try {
            return objectMapper.readValue(rs.getString(columnName), new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<ItemFlag> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        try {
            return objectMapper.readValue(rs.getString(columnIndex), new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<ItemFlag> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        try {
            return objectMapper.readValue(cs.getString(columnIndex), new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
