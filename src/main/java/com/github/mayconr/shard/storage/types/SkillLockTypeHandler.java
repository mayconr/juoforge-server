package com.github.mayconr.shard.storage.types;

import com.github.mayconr.juoserver.game.model.SkillLock;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedJdbcTypes(JdbcType.INTEGER)
@MappedTypes(SkillLock.class)
public class SkillLockTypeHandler extends BaseTypeHandler<SkillLock> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, SkillLock parameter, JdbcType jdbcType) throws SQLException {
        ps.setInt(i, parameter.getCode());
    }

    @Override
    public SkillLock getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return SkillLock.fromCode(rs.getInt(columnName));
    }

    @Override
    public SkillLock getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return SkillLock.fromCode(rs.getInt(columnIndex));
    }

    @Override
    public SkillLock getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return SkillLock.fromCode(cs.getInt(columnIndex));
    }
}
