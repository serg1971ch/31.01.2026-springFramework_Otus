package ru.otus;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import ru.otus.service.TestRunnerService;

public class Application {
    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("spring-context.xml");
        TestRunnerService testRunnerService = context.getBean(TestRunnerService.class);

        testRunnerService.run();
    }
}