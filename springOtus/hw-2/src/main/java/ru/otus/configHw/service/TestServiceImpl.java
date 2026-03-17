package ru.otus.configHw.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.otus.configHw.dao.QuestionDao;
import ru.otus.configHw.domain.Answer;
import ru.otus.configHw.domain.Question;
import ru.otus.configHw.domain.Student;
import ru.otus.configHw.domain.TestResult;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class TestServiceImpl implements TestService {

    private final IOService ioService;

    private final QuestionDao questionDao;

    @Override
    public TestResult executeTestFor(Student student) {
        ioService.printLine("");
        ioService.printFormattedLine("Hello, %s!", student.getFullName());
        List<Question> questions = questionDao.findAll();
        ioService.printLine("");
        ioService.printLine("Please answer the questions below:");
        int qNum = 1;
        TestResult testResult = new TestResult(student);
        for (Question question : questions) {
            ioService.printFormattedLine("%d) %s", qNum++, question.text());
            List<Answer> answers = question.answers();
            if (answers == null || answers.isEmpty()) {
                ioService.printLine("No answers available for this question.");
                continue;
            }
            for (int i = 0; i < answers.size(); i++) {
                ioService.printFormattedLine(" %d) %s", i + 1, answers.get(i).text());
            }

            ioService.printLine("Your answer (numbers separated by comma, e.g., 1,2,3): ");
            String line = ioService.readString();
            if (line == null || line.trim().isEmpty()) {
                ioService.printLine("Answer cannot be empty. Please enter numbers (e.g., 1,2,3).");
                continue;
            }
            try {
                Set<Integer> chosenIndices = Arrays.stream(line.trim().split("\\s*,\\s*"))
                        .filter(s -> !s.isBlank())
                        .map(String::trim)
                        .map(Integer::parseInt)
                        .map(i -> i - 1)
                        .collect(Collectors.toSet());
                if (chosenIndices.isEmpty() || chosenIndices.size() > answers.size()) {
                    ioService.printLine("Please enter from 1 to " + answers.size() + " numbers (e.g., 1,2,3).");
                    continue;
                }
                Set<Integer> expectedIndices = IntStream.range(0, answers.size())
                        .filter(i -> answers.get(i).isCorrect())
                        .boxed()
                        .collect(Collectors.toSet());
                boolean isRight = chosenIndices.equals(expectedIndices);
                testResult.applyAnswer(question, isRight);
                break;
            } catch (NumberFormatException ex) {
                ioService.printLine("Invalid number format. Please enter numbers separated by comma (e.g., 1,2,3).");
            }
        }
        return testResult;
    }
}
