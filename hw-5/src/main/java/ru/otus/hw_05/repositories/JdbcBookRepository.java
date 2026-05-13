package ru.otus.hw_05.repositories;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.otus.hw_05.models.Author;
import ru.otus.hw_05.models.Book;
import ru.otus.hw_05.models.Genre;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class JdbcBookRepository implements BookRepository {

    private final JdbcTemplate jdbc;
    private final NamedParameterJdbcTemplate named;
//    private final JdbcAuthorRepository authorRepo;

    public JdbcBookRepository(JdbcTemplate jdbc,
                              NamedParameterJdbcTemplate named) {
        this.jdbc = jdbc;
        this.named = named;
    }

    @Override
    public Optional<Book> findById(long id) {
        String bookSql = "select b.id as book_id, b.title, b.author_id, a.full_name " +
                "from books b left join authors a on b.author_id = a.id where b.id = ?";
        List<Book> list = jdbc.query(bookSql, (rs, rn) -> {
            long bookId = rs.getLong("book_id");
            String title = rs.getString("title");
            long authorId = rs.getLong("author_id");
            Author author = new Author(authorId, rs.getString("full_name"));
            Book book = new Book();
            book.setId(bookId);
            book.setTitle(title);
            book.setAuthor(author);
            book.setGenres(new ArrayList<>());
            return book;
        }, id);

        if (list.isEmpty()) return Optional.empty();

        Book book = list.get(0);
        book.setGenres(loadGenresForBook(book.getId()));
        return Optional.of(book);
    }

    @Override
    public List<Book> findAll() {
        // Получаем все книги + авторов
        String sql = "select b.id as book_id, b.title, b.author_id, a.full_name " +
                "from books b left join authors a on b.author_id = a.id";
        List<Book> books = jdbc.query(sql, (rs, rn) -> {
            Book b = new Book();
            b.setId(rs.getLong("book_id"));
            b.setTitle(rs.getString("title"));
            long authorId = rs.getLong("author_id");
            b.setAuthor(new Author(authorId, rs.getString("full_name")));
            b.setGenres(new ArrayList<>());
            return b;
        });

        if (books.isEmpty()) return books;

        // собрать genres для всех книг одной выборкой
        List<Long> bookIds = books.stream().map(Book::getId).collect(Collectors.toList());
        String inSql = "select bg.book_id, g.id, g.name from books_genres bg " +
                "join genres g on bg.genre_id = g.id where bg.book_id in (:ids)";
        MapSqlParameterSource params = new MapSqlParameterSource("ids", bookIds);
        Map<Long, List<Genre>> map = named.query(inSql, params, rs -> {
            Map<Long, List<Genre>> m = new HashMap<>();
            while (rs.next()) {
                long bookId = rs.getLong("book_id");
                Genre g = new Genre(rs.getLong("id"), rs.getString("name"));
                m.computeIfAbsent(bookId, k -> new ArrayList<>()).add(g);
            }
            return m;
        });

        // назначить жанры книгам
        for (Book b : books) {
            b.setGenres(map.getOrDefault(b.getId(), Collections.emptyList()));
        }
        return books;
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
}
