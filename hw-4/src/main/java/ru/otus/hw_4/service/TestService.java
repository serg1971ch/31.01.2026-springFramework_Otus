package ru.otus.hw_4.service;

import ru.otus.hw_4.domain.Student;
import ru.otus.hw_4.domain.TestResult;

public interface TestService {
    TestResult executeTestFor(Student student);
}
