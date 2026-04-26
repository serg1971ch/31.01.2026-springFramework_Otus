package ru.otus.hw_4.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.otus.hw_4.dao.QuestionDao;
import ru.otus.hw_4.domain.Answer;
import ru.otus.hw_4.domain.Question;
import ru.otus.hw_4.domain.Student;
import ru.otus.hw_4.domain.TestResult;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class TestServiceImpl implements TestService {

    private final ResultService resultService;
    private final LocalizedIOService ioService;
    private final QuestionDao questionDao;

    @Override
    public TestResult executeTestFor(Student student) {
        ioService.printLine("");
        ioService.printFormattedLineLocalized("TestService.answer.the.questions");
        ioService.printLine("");

        List<Question> questions = questionDao.findAll();
        TestResult testResult = new TestResult(student);

        int qNum = 1;
        for (Question question : questions) {
            displayQuestion(question, qNum++);
            boolean answeredCorrectly = processUserAnswerWithRetries(question, testResult);
            testResult.applyAnswer(question, answeredCorrectly);
        }

        return testResult;
    }

    public void displayQuestion(Question question, int qNum) {
        ioService.printFormattedLine("%d) %s", qNum, question.text());

        List<Answer> answers = question.answers();
        if (answers == null || answers.isEmpty()) {
            ioService.printLineLocalized("TestService.no.answers.available");
            return;
        }

        for (int i = 0; i < answers.size(); i++) {
            ioService.printFormattedLine(" %d) %s", i + 1, answers.get(i).text());
        }
    }

    public boolean processUserAnswerWithRetries(Question question, TestResult testResult) {
        List<Answer> answers = question.answers();
        if (answers == null || answers.isEmpty()) {
            return false;
        }

        boolean isRight = false;
        int totalAnswers = answers.size();

        int min = 1;
        int max = 3;
        String errorMessage = "Enter number correct answer number";

        String line = String.valueOf(ioService.readIntForRange(min, max, errorMessage));

        Set<Integer> chosenIndices = parseUserInput(line, totalAnswers);

        isRight = checkAnswerCorrectness(chosenIndices, answers);
        if (isRight) {
            ioService.printLineLocalized("TestService.correct.answer");
            return true;
        } else {
            ioService.printLineLocalized("TestService.incorrect.answer");
            return false;
        }
    }

    public Set<Integer> parseUserInput(String line, int totalAnswers) {
        return Arrays.stream(line.trim().split("\\s*,\\s*"))
                .filter(s -> !s.isBlank())
                .map(String::trim)
                .map(Integer::parseInt)
                .map(i -> i - 1) // преобразуем в индексы (1 → 0, 2 → 1 и т. д.)
                .collect(Collectors.toSet());
    }

    public boolean checkAnswerCorrectness(Set<Integer> chosenIndices, List<Answer> answers) {
        Set<Integer> expectedIndices = IntStream.range(0, answers.size())
                .filter(i -> answers.get(i).isCorrect())
                .boxed()
                .collect(Collectors.toSet());
        return chosenIndices.equals(expectedIndices);
    }
}
