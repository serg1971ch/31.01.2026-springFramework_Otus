package ru.otus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;
import ru.otus.dao.CsvQuestionDao;
import ru.otus.domain.Answer;
import ru.otus.domain.Message;
import ru.otus.domain.Question;
import ru.otus.service.IOService;
import ru.otus.service.MessageService;
import ru.otus.service.TestServiceImpl;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TestServiceImplTest {

    @Mock
    private IOService ioService;

    @Mock
    private CsvQuestionDao questionDao;

    @Mock
    private MessageService messageService;

    @InjectMocks
    private TestServiceImpl testService;
    private List<Question> questions;

    @BeforeEach
    void setUp() {
        Answer answer1 = new Answer("Paris", true);
        Answer answer2 = new Answer("London", false);
        Answer answer3 = new Answer("Berlin", false);

        Question question = new Question(
                "What is capital of France?",
                List.of(answer1, answer2, answer3)
        );
        questions = List.of(question);
    }

    @Test
    void executeTest_correctInput_shouldPrintCorrect() {
        when(messageService.getNameForMessage()).thenReturn("John");
        when(questionDao.findAll()).thenReturn(questions);
        Message mockMessage = new Message();
        mockMessage.setMessage("Hello, John!");
        when(messageService.getMessage(0)).thenReturn(mockMessage);
        when(ioService.readLine()).thenReturn("1");

        doNothing().when(ioService).printLine(any(String.class));
        doNothing().when(ioService).printFormattedLine(any(String.class), any());

        testService.executeTest();
        verify(ioService).printFormattedLine("Hello, %s!", "John");
        verify(ioService).printLine("Please answer the questions below:");
        verify(ioService, times(1)).printLine("Correct!");
    }

    @Test
    void executeTest_invalidNumber_shouldShowErrorAndRepeatQuestion() {
        // Настройки зависимостей
        when(messageService.getNameForMessage()).thenReturn("Anna");
        when(questionDao.findAll()).thenReturn(questions);

        when(ioService.readLine())
                .thenReturn("abc")
                .thenReturn("1");
        when(messageService.getMessage(anyInt())).thenAnswer(invocation -> {
            int index = invocation.getArgument(0);
            Message message = new Message();
            message.setMessage("Mock message for index " + index);
            return message;
        });

        testService.executeTest();

        InOrder inOrder = inOrder(ioService);
        inOrder.verify(ioService).printFormattedLine("Hello, %s!", "Anna");
        inOrder.verify(ioService).printLine("");
        inOrder.verify(ioService).printLine("Please answer the questions below:");

        inOrder.verify(ioService).printFormattedLine("%d) %s", 1, "What is capital of France?");
        inOrder.verify(ioService).printFormattedLine(" %d) %s", 1, "Paris");
        inOrder.verify(ioService).printFormattedLine(" %d) %s", 2, "London");
        inOrder.verify(ioService).printFormattedLine(" %d) %s", 3, "Berlin");

        inOrder.verify(ioService).printLine("Your answer (numbers separated by comma, e.g., 1,2,3): ");
        inOrder.verify(ioService).printLine(argThat(msg ->
                msg != null && msg.contains("Invalid input") && msg.contains("3")
        ));

        inOrder.verify(ioService).printLine("Your answer (numbers separated by comma, e.g., 1,2,3): ");
        inOrder.verify(ioService).printLine("Correct!");
        inOrder.verify(ioService).printLine("");

        // Финальное сообщение (зависит от totalErrors)
        inOrder.verify(ioService).printLine("Mock message for index 0");

        verifyNoMoreInteractions(ioService);
    }

    @Test
    void executeTest_emptyInput_shouldShowError() {
        when(messageService.getNameForMessage()).thenReturn("Pitt");

        Answer correctAnswer = new Answer("Paris", true);
        Answer wrongAnswer = new Answer("London", false);
        List<Answer> answers = Arrays.asList(correctAnswer, wrongAnswer);
        Question question = new Question("What is capital of France?", answers);
        List<Question> questions = Arrays.asList(question);
        when(questionDao.findAll()).thenReturn(questions);

        when(messageService.getMessage(anyInt())).thenAnswer(invocation -> {
            int index = invocation.getArgument(0);
            Message message = new Message();
            message.setMessage("Mock message for index " + index);
            return message;
        });

        when(ioService.readLine())
                .thenReturn("")     // первый ввод — пустой (вызывает ошибку)
                .thenReturn("1");  // второй ввод — корректный (завершает тест)

        // Выполняем тест
        testService.executeTest();

        InOrder inOrder = inOrder(ioService);

        // Приветствие
        inOrder.verify(ioService).printFormattedLine("Hello, %s!", "Pitt");
        inOrder.verify(ioService).printLine("");
        inOrder.verify(ioService).printLine("Please answer the questions below:");

        // Вопрос и варианты ответов
        inOrder.verify(ioService).printFormattedLine("%d) %s", 1, "What is capital of France?");
        inOrder.verify(ioService).printFormattedLine(" %d) %s", 1, "Paris");
        inOrder.verify(ioService).printFormattedLine(" %d) %s", 2, "London");

        // Первый запрос ввода
        inOrder.verify(ioService).printLine("Your answer (numbers separated by comma, e.g., 1,2,3): ");

        // Сообщение об ошибке для пустого ввода
        inOrder.verify(ioService).printLine("Answer cannot be empty. Please enter numbers (e.g., 1,2,3).");

        // Повторный запрос ввода после ошибки
        inOrder.verify(ioService).printLine("Your answer (numbers separated by comma, e.g., 1,2,3): ");
        // Обработка корректного ввода: сообщение о результате
        inOrder.verify(ioService).printLine("Correct!");
        // Пустая строка после ответа
        inOrder.verify(ioService).printLine("");
        inOrder.verify(ioService).printLine("Mock message for index 0");

        verifyNoMoreInteractions(ioService);
    }

    @Test
    void executeTest_tooManyAnswers_shouldShowError() {
        when(messageService.getNameForMessage()).thenReturn("Mary");

        Answer answer1 = new Answer("Option 1", false);
        Answer answer2 = new Answer("Option 2", false);
        Answer answer3 = new Answer("Option 3", false);
        Answer answer4 = new Answer("Option 4", true);
        List<Answer> answers = Arrays.asList(answer1, answer2, answer3, answer4);
        Question question = new Question("Sample question?", answers);
        List<Question> questions = Arrays.asList(question);
        when(questionDao.findAll()).thenReturn(questions);

        when(messageService.getMessage(anyInt())).thenAnswer(invocation -> {
            int index = invocation.getArgument(0);
            Message message = new Message();
            message.setMessage("Mock message for index " + index);
            return message;
        });

        when(ioService.readLine())
                .thenReturn("1,2,3,4,5")  // первый ввод — 5 чисел (вызывает ошибку)
                .thenReturn("1");         // второй ввод — корректный (завершает тест)

        testService.executeTest();

        InOrder inOrder = inOrder(ioService);
        inOrder.verify(ioService).printFormattedLine("Hello, %s!", "Mary");
        inOrder.verify(ioService).printLine("");
        inOrder.verify(ioService).printLine("Please answer the questions below:");

        inOrder.verify(ioService).printFormattedLine("%d) %s", 1, "Sample question?");
        inOrder.verify(ioService).printFormattedLine(" %d) %s", 1, "Option 1");
        inOrder.verify(ioService).printFormattedLine(" %d) %s", 2, "Option 2");
        inOrder.verify(ioService).printFormattedLine(" %d) %s", 3, "Option 3");
        inOrder.verify(ioService).printFormattedLine(" %d) %s", 4, "Option 4");

        inOrder.verify(ioService).printLine("Your answer (numbers separated by comma, e.g., 1,2,3): ");
        inOrder.verify(ioService).printLine("Please enter from 1 to 4 numbers (e.g., 1,2,3).");
        inOrder.verify(ioService).printLine("Your answer (numbers separated by comma, e.g., 1,2,3): ");
        inOrder.verify(ioService, atLeastOnce()).printLine(anyString());

        verifyNoMoreInteractions(ioService);
    }

    @Test
    void executeTest_incorrectAnswer_shouldPrintIncorrect() {
        when(messageService.getNameForMessage()).thenReturn("Alex");
        when(questionDao.findAll()).thenReturn(questions);
        when(ioService.readLine()).thenReturn("2");

        when(messageService.getMessage(anyInt())).thenAnswer(invocation -> {
            int index = invocation.getArgument(0);
            Message message = new Message();
            message.setMessage("Mock message for index " + index);
            return message;
        });

        testService.executeTest();
        verify(ioService, times(1)).printLine("Incorrect.");
    }
}

