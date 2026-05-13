package ru.otus.hw_4.dao;


import ru.otus.hw_4.domain.Question;

import java.util.List;

public interface QuestionDao {
    List<Question> findAll();
}
