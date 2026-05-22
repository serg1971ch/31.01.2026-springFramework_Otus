package ru.otus.hw_05.repositories;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.otus.hw_05.models.Author;
import ru.otus.hw_05.models.Book;
import ru.otus.hw_05.models.Genre;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Repository
public class JdbcBookRepository implements BookRepository {

    private final JdbcTemplate jdbc;
    private final NamedParameterJdbcTemplate named;


    @Override
    public Optional<Book> findById(long id) {
        String sql = "SELECT b.id AS book_id, b.title, a.id AS author_id, a.full_name " +
                "FROM books b LEFT JOIN authors a ON b.author_id = a.id WHERE b.id = ?";

        List<Book> list = jdbc.query(sql, (rs, rn) -> mapBookFromResultSet(rs), id);

        if (list.isEmpty()) return Optional.empty();

        Book book = list.get(0);
        book.setGenres(loadGenresForBook(book.getId()));
        return Optional.of(book);
    }

    @Override
    public List<Book> findAll() {
        String sql = "SELECT " +
                "b.id AS book_id, b.title, " +
                "a.id AS author_id, a.full_name, " +
                "g.id AS genre_id, g.name AS genre_name " +
                "FROM books b " +
                "LEFT JOIN authors a ON b.author_id = a.id " +
                "LEFT JOIN books_genres bg ON b.id = bg.book_id " +
                "LEFT JOIN genres g ON bg.genre_id = g.id " +
                "ORDER BY b.id";

        return jdbc.query(sql, new FullBookResultSetExtractor());
    }

    private static class FullBookResultSetExtractor implements ResultSetExtractor<List<Book>> {
        @Override
        public List<Book> extractData(ResultSet rs) throws SQLException {
            List<Book> books = new ArrayList<>();
            Map<Long, Book> bookMap = new HashMap<>();

            while (rs.next()) {
                Long bookId = rs.getLong("book_id");

                Book book = bookMap.computeIfAbsent(bookId, k -> {
                    Book b = new Book();
                    b.setId(bookId);
                    try {
                        b.setTitle(rs.getString("title"));
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }

                    long authorId = 0;
                    try {
                        authorId = rs.getLong("author_id");
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        if (!rs.wasNull()) {
                            Author author = new Author(authorId, rs.getString("full_name"));
                            b.setAuthor(author);
                        } else {
                            b.setAuthor(null);
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }

                    b.setGenres(new ArrayList<>());
                    return b;
                });

                Long genreId = rs.getLong("genre_id");
                if (!rs.wasNull() && genreId != 0) {
                    Genre genre = new Genre(genreId, rs.getString("genre_name"));
                    if (!book.getGenres().contains(genre)) {
                        book.getGenres().add(genre);
                    }
                }
            }

            books.addAll(bookMap.values());
            return books;
        }
    }


    @Override
    public Book save(Book book) {
        if (book.getId() == 0) {
            // insert
            SimpleJdbcInsert insert = new SimpleJdbcInsert(jdbc)
                    .withTableName("books")
                    .usingGeneratedKeyColumns("id");
            Map<String, Object> params = new HashMap<>();
            params.put("title", book.getTitle());
            params.put("author_id", book.getAuthor() != null ? book.getAuthor().getId() : null);
            Number newId = insert.executeAndReturnKey(new MapSqlParameterSource(params));
            book.setId(newId.longValue());
        } else {
            String sql = "update books set title = ?, author_id = ? where id = ?";
            jdbc.update(sql, book.getTitle(),
                    book.getAuthor() != null ? book.getAuthor().getId() : null,
                    book.getId());
            // очистим текущие связи жанров
            jdbc.update("delete from books_genres where book_id = ?", book.getId());
        }

        // вставим связи с жанрами
        if (book.getGenres() != null && !book.getGenres().isEmpty()) {
            String insertRel = "insert into books_genres(book_id, genre_id) values (?, ?)";
            for (Genre g : book.getGenres()) {
                jdbc.update(insertRel, book.getId(), g.getId());
            }
        }
        return book;
    }

    @Override
    public void deleteById(long id) {
        jdbc.update("delete from books where id = ?", id);
    }

    private List<Genre> loadGenresForBook(long bookId) {
        String sql = "select g.id, g.name from books_genres bg join genres g on bg.genre_id = g.id where bg.book_id = ?";
        return jdbc.query(sql, (rs, rn) -> new Genre(rs.getLong("id"), rs.getString("name")), bookId);
    }

    private Book mapBookFromResultSet(ResultSet rs) throws SQLException {
        Book book = new Book();
        book.setId(rs.getLong("book_id"));
        book.setTitle(rs.getString("title"));

        long authorId = rs.getLong("author_id");
        if (!rs.wasNull()) {
            Author author = new Author(authorId, rs.getString("full_name"));
            book.setAuthor(author);
        } else {
            book.setAuthor(null);
        }

        book.setGenres(new ArrayList<>());
        return book;
    }
}
