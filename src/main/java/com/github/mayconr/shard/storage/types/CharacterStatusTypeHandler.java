package com.github.mayconr.shard.storage.types;

import com.github.mayconr.juoserver.game.model.CharacterStatus;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CharacterStatusTypeHandler extends BaseTypeHandler<CharacterStatus> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, CharacterStatus parameter, JdbcType jdbcType) throws SQLException {
        ps.setInt(i, parameter.getCode());
    }

    @Override
    public CharacterStatus getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return CharacterStatus.fromCode(rs.getInt(columnName));
    }

    @Override
    public CharacterStatus getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return CharacterStatus.fromCode(rs.getInt(columnIndex));
    }

    @Override
    public CharacterStatus getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return CharacterStatus.fromCode(cs.getInt(columnIndex));
    }
}
