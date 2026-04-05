package ru.otus.hw_03.dao;


import ru.otus.hw_03.domain.Question;

import java.util.List;

public interface QuestionDao {
    List<Question> findAll();
}
