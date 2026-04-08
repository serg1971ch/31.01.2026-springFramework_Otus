package ru.otus.hw_03.domain;

import java.util.List;

public record Question(String text, List<Answer> answers) {
}
