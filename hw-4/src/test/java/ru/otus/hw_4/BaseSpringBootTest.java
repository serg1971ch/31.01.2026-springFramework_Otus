package ru.otus.hw_4;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.shell.interactive.enabled=false",
        "test.right-answers-count-to-pass=3",
        "test.locale=ru",
        "test.file-name-by-locale-tag.ru=test_ru.csv"
})
public abstract class BaseSpringBootTest {
}
