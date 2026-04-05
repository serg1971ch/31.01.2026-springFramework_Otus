package ru.otus.hw_03.service;

import ru.otus.hw_03.domain.Student;
import ru.otus.hw_03.domain.TestResult;

public interface TestService {
    TestResult executeTestFor(Student student);
}
