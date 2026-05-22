package ru.otus.hw_05.repositories;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.otus.hw_05.models.Genre;


import java.util.Collections;
import java.util.List;
import java.util.Set;

@Repository
public class JdbcGenreRepository implements GenreRepository {

    private final JdbcTemplate jdbc;
    private final NamedParameterJdbcTemplate named;

    public JdbcGenreRepository(JdbcTemplate jdbc, NamedParameterJdbcTemplate named) {
        this.jdbc = jdbc;
        this.named = named;
    }

    @Override
    public List<Genre> findAll() {
        String sql = "select id, name from genres";
        return jdbc.query(sql, (rs, rowNum) -> {
            Genre g = new Genre();
            g.setId(rs.getLong("id"));
            g.setName(rs.getString("name"));
            return g;
        });
    }

    @Override
    public List<Genre> findAllByIds(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        String sql = "select id, name from genres where id in (:ids)";
        MapSqlParameterSource params = new MapSqlParameterSource("ids", ids);
        return named.query(sql, params, (rs, rowNum) -> {
            Genre g = new Genre();
            g.setId(rs.getLong("id"));
            g.setName(rs.getString("name"));
            return g;
        });
    }
}
