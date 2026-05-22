package ru.otus.hw_05.repositories;

import ru.otus.hw_05.models.Author;

import java.util.List;
import java.util.Optional;

public interface AuthorRepository {
    List<Author> findAll();

    Optional<Author> findById(long id);
}
