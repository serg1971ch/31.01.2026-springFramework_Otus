package ru.otus.service;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TestRunnerServiceImpl implements TestRunnerService {

    private final ru.otus.service.TestService testService;

    @Override
    public void run() {
        testService.executeTest();
    }
}
