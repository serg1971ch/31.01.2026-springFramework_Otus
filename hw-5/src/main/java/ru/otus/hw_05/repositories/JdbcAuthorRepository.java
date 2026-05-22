package ru.otus.hw_05.repositories;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.otus.hw_05.models.Author;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcAuthorRepository implements AuthorRepository {

    private final JdbcTemplate jdbc;

    public JdbcAuthorRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<Author> findAll() {
        String sql = "select id, full_name from authors";
        return jdbc.query(sql, new AuthorRowMapper());
    }

    @Override
    public Optional<Author> findById(long id) {
        String sql = "select id, full_name from authors where id = ?";
        List<Author> list = jdbc.query(sql, new AuthorRowMapper(), id);
        return list.stream().findFirst();
    }

    private static class AuthorRowMapper implements RowMapper<Author> {
        @Override
        public Author mapRow(ResultSet rs, int rowNum) throws SQLException {
            Author a = new Author();
            a.setId(rs.getLong("id"));
            a.setFullName(rs.getString("full_name"));
            return a;
        }
    }
}