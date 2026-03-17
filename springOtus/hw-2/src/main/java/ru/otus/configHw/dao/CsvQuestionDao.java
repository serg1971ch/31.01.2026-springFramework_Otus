package ru.otus.configHw.dao;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.otus.configHw.config.TestFileNameProvider;
import ru.otus.configHw.dao.dto.QuestionDto;
import ru.otus.configHw.domain.Question;
import ru.otus.configHw.exceptions.QuestionReadException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class CsvQuestionDao implements QuestionDao {
    private final TestFileNameProvider fileNameProvider;

    @Override
    public List<Question> findAll() {
        String fileName = fileNameProvider.getTestFileName();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(fileName)) {
            if (is == null) {
                throw new QuestionReadException("File not found: " + fileName);
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
