package ru.otus.configHw.domain;

import java.util.List;

public record Question(String text, List<Answer> answers) {
}
