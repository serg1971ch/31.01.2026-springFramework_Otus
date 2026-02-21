package ru.otus.service;

import lombok.RequiredArgsConstructor;
import ru.otus.dao.CsvQuestionDao;
import ru.otus.domain.Answer;
import ru.otus.domain.Question;
import ru.otus.exceptions.QuestionReadException;

import java.util.List;

@RequiredArgsConstructor
public class TestServiceImpl implements TestService {

    private final IOService ioService;
    private final CsvQuestionDao questionDao;
    private final MessageService messageService;

    @Override
    public void executeTest() {

        String fullName = messageService.getNameForMessage();
        ioService.printFormattedLine("Hello, %s!", fullName);

        ioService.printLine("");
        ioService.printFormattedLine("Please answer the questions below%n");

        try {
            List<Question> questions = questionDao.findAll();
            int qNum = 0;
            for (Question question : questions) {
                ioService.printFormattedLine("%d. %s", qNum++, question.text());
                List<Answer> answers = question.answers();
                for (int i = 0; i < answers.size(); i++) {
                    ioService.printFormattedLine(" %d. %s", i + 1, answers.get(i).text());
                }
                ioService.printFormattedLine("%n");
            }
        } catch (QuestionReadException e) {
            ioService.printLine("Error reading questions: " + e.getMessage());
        }
    }
}
