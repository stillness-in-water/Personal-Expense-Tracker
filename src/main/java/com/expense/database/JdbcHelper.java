package com.expense.database;

import com.expense.interfaces.RowMapper;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;


public final class JdbcHelper {

    private JdbcHelper() {
    }

    public static <T> List<T> toList(ResultSet rs, RowMapper<T> mapper)
            throws SQLException {

        List<T> result = new ArrayList<>();

        while (rs.next()) {
            result.add(mapper.map(rs));
        }

        return result;
    }

    public static void setNullableInt(PreparedStatement ps,
                                      int index,
                                      Integer value) throws SQLException {

        if (value == null) {
            ps.setNull(index, Types.INTEGER);
        } else {
            ps.setInt(index, value);
        }
    }
}
