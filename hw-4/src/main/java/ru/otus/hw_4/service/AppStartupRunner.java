package ru.otus.hw_4.service;


import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AppStartupRunner implements CommandLineRunner {

    private final TestRunnerService testRunnerService;

    @Override
    public void run(String... args) {
        testRunnerService.run();
    }
}
