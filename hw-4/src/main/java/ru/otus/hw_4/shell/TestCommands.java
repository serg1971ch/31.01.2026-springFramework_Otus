package ru.otus.hw_4.shell;

import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import ru.otus.hw_4.service.StudentService;
import ru.otus.hw_4.service.TestRunnerService;
import ru.otus.hw_4.service.TestService;
import ru.otus.hw_4.domain.Student;
import ru.otus.hw_4.domain.TestResult;
import lombok.RequiredArgsConstructor;

@ShellComponent
@RequiredArgsConstructor
public class TestCommands {

    private final TestRunnerService testRunnerService;

    @ShellMethod(key = "start-test", value = "Start the test for a student")
    public String startTest() {
        try {
            testRunnerService.run();
            return "Test started successfully!";
        } catch (Exception e) {
            return "Error during test execution: " + e.getMessage();
        }
    }
}

