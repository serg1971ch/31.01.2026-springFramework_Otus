package ru.otus.configHw.config;


import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Value;

@Configuration
@ComponentScan(basePackages = "ru.otus.configHw.dao")
@PropertySource("classpath:application.properties")
public class AppConfiguration {

    @Bean
    public AppProperties appProperties(
            @Value("${app.right.answers.count}") int rightAnswersCountToPass,
            @Value("${app.file.name}") String testFileName) {
        return new AppProperties(rightAnswersCountToPass, testFileName);
    }
}

