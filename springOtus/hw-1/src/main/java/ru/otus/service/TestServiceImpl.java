package ru.otus.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import ru.otus.dao.CsvQuestionDao;
import ru.otus.domain.Answer;
import ru.otus.domain.Question;

import java.util.*;
import java.util.stream.Collectors;

@Log
@RequiredArgsConstructor
public class TestServiceImpl implements TestService {

    private final IOService ioService;
    private final CsvQuestionDao questionDao;
    private final MessageService messageService;

    @Override
    public void executeTest() {
        String fullName = messageService.getNameForMessage();
        ioService.printFormattedLine("Hello, %s!", fullName);

        List<Question> questions = questionDao.findAll();
        ioService.printLine("");
        ioService.printLine("Please answer the questions below:");

        int qNum = 1;
        int totalErrors = 0;

        try {
            for (Question question : questions) {
                ioService.printFormattedLine("%d) %s", qNum++, question.text());
                List<Answer> answers = question.answers();

                if (answers.isEmpty()) {
                    ioService.printLine("No answers available for this question.");
                    continue;
                }

                for (int i = 0; i < answers.size(); i++) {
                    ioService.printFormattedLine(" %d) %s", i + 1, answers.get(i).text());
                }

                // Цикл для повторного запроса при ошибке
                while (true) {
                    ioService.printLine("Your answer (numbers separated by comma, e.g., 1,2,3): ");
                    String line = ioService.readLine();

                    if (line == null || line.trim().isEmpty()) {
                        ioService.printLine("Answer cannot be empty. Please enter numbers (e.g., 1,2,3).");
                        continue; // Повторный запрос
                    }

                    try {
                        Set<Integer> chosenIndices = Arrays.stream(line.trim().split("\\s*,\\s*"))
                                .filter(s -> !s.isBlank())
                                .map(String::trim)
                                .map(Integer::parseInt)
                                .map(i -> i - 1)
                                .collect(Collectors.toSet());

                        int count = chosenIndices.size();
                        if (count < 1 || count > 4) {
                            ioService.printLine("Please enter from 1 to 4 numbers (e.g., 1,2,3).");
                            continue; // Повторный запрос
                        }

                        int maxAnswerIndex = answers.size() - 1;
                        if (chosenIndices.stream().anyMatch(idx -> idx < 0 || idx > maxAnswerIndex)) {
                            ioService.printLine("Invalid answer number. Please enter numbers from 1 to " + answers.size());
                            continue; // Повторный запрос
                        }

                        // Подсчёт ошибок
                        int errorsInQuestion = 0;
                        for (int i = 0; i < answers.size(); i++) {
                            boolean correct = answers.get(i).isCorrect();
                            boolean selected = chosenIndices.contains(i);
                            if (correct != selected) {
                                errorsInQuestion++;
                            }
                        }

                        totalErrors += errorsInQuestion;
                        ioService.printLine(errorsInQuestion == 0 ? "Correct!" : "Incorrect.");
                        ioService.printLine("");
                        break; // Выход из цикла при корректном вводе

                    } catch (NumberFormatException e) {
                        ioService.printLine("Invalid input. Please enter numbers separated by commas from 1 to " + answers.size() + ".");
                        continue; // Повторный запрос
                    }
                }
                // Конец цикла while(true)
            }
            // Конец цикла for(Question question : questions)

            // Вывод итогового сообщения ПОСЛЕ всех вопросов
            ioService.printLine(messageService.getMessage(totalErrors).getMessage());

        } catch (Exception e) {
            ioService.printLine("An error occurred while reading input. Test interrupted.");
            log.severe("Input error: " + e.getMessage());
        }
    }
}
