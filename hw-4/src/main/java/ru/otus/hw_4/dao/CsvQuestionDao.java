package ru.otus.hw_4.dao;

import com.opencsv.bean.CsvToBeanBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.otus.hw_4.config.TestFileNameProvider;
import ru.otus.hw_4.dao.dto.QuestionDto;
import ru.otus.hw_4.domain.Question;
import ru.otus.hw_4.exceptions.QuestionReadException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class CsvQuestionDao implements QuestionDao {
    private final TestFileNameProvider fileNameProvider;

    @Override
    public List<Question> findAll() {
        String fileName = fileNameProvider.getTestFileName();

        if (fileName == null || fileName.trim().isEmpty()) {
            throw new QuestionReadException(
                    "Test file name is null or empty. Check configuration of TestFileNameProvider.");
        }

        try (InputStream is = getClass().getClassLoader().getResourceAsStream(fileName)) {
            if (is == null) {
                throw new QuestionReadException("File not found: " + fileName +
                        ". Available resources: " + Arrays.toString(getClass().getClassLoader().getResources("").nextElement().toString().toCharArray()));
            }
            try (Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                return new CsvToBeanBuilder<QuestionDto>(reader)
                        .withType(QuestionDto.class)
                        .withIgnoreLeadingWhiteSpace(true)
                        .build()
                        .parse()
                        .stream()
                        .map(QuestionDto::toDomainObject)
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            throw new QuestionReadException("Failed to read questions from file " + fileName, e);
        }
    }
}
