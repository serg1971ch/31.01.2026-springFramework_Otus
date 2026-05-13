package ru.otus.hw_05.services;


import ru.otus.hw_05.models.Author;

import java.util.List;

public interface AuthorService {
    List<Author> findAll();
}
