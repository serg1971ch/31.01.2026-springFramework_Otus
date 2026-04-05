package ru.otus.configHw.service;

import ru.otus.configHw.domain.Student;
import ru.otus.configHw.domain.TestResult;

public interface TestService {
    TestResult executeTestFor(Student student);
}
