package ru.otus.configHw.config;

import lombok.Data;


import lombok.Data;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
public class AppProperties implements TestConfig, TestFileNameProvider {
    private final int rightAnswersCountToPass;
    private final String testFileName;
}

