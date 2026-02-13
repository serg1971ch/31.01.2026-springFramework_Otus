package ru.otus.dao;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.RequiredArgsConstructor;
import ru.otus.config.TestFileNameProvider;
import ru.otus.dao.dto.QuestionDto;
import ru.otus.domain.Question;
import ru.otus.exceptions.QuestionReadException;


import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
                CsvToBean<QuestionDto> csv = new CsvToBeanBuilder<QuestionDto>(reader)
                        .withType(QuestionDto.class)
                        .withIgnoreLeadingWhiteSpace(true)
                        .build();
                List<QuestionDto> dtos = csv.parse();
                return dtos.stream()
                        .map(QuestionDto::toDomainObject)
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            throw new QuestionReadException("Failed to read questions from file " + fileName, e);
        }
    }
}

