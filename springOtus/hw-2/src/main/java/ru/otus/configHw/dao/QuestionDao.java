package ru.otus.configHw.dao;

import ru.otus.configHw.domain.Question;

import java.util.List;

public interface QuestionDao {
    List<Question> findAll();
}
