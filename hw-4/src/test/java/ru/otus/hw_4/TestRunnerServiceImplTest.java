package ru.otus.hw_4;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.shell.InputProvider;
import org.springframework.shell.ResultHandlerService;
import org.springframework.shell.Shell;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import ru.otus.hw_4.dao.QuestionDao;
import ru.otus.hw_4.domain.Answer;
import ru.otus.hw_4.domain.Question;
import ru.otus.hw_4.domain.Student;
import ru.otus.hw_4.domain.TestResult;
import ru.otus.hw_4.service.LocalizedIOService;
import ru.otus.hw_4.service.ResultService;
import ru.otus.hw_4.service.TestServiceImpl;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@SpringBootTest
@DisplayName("Тест TestRunnerServiceImpl")
class TestRunnerServiceImplTest {

    private static final String GREETING_PATTERN = "Добро пожаловать: %s";
    private static final String DEFAULT_LOGIN = "AnyUser";
    private static final String CUSTOM_LOGIN = "Вася";
    private static final String COMMAND_LOGIN = "login";
    private static final String COMMAND_LOGIN_SHORT = "l";
    private static final String COMMAND_LOGIN_PATTERN = "%s %s";

    @MockBean
    private ResultService resultService;

    @MockBean
    private LocalizedIOService ioService;

    @MockBean
    private QuestionDao questionDao;

    @Autowired
    private TestServiceImpl testService;

    private Student student;
    private List<Question> questions;

    @MockitoSpyBean
    private ResultHandlerService resultHandlerService;

    private InputProvider inputProvider;

    private ArgumentCaptor<Object> argumentCaptor;

    @Autowired
    private Shell shell;

    @BeforeEach
    void setUp() {
        inputProvider = mock(InputProvider.class);
        argumentCaptor = ArgumentCaptor.forClass(Object.class);

        student = new Student("Ivan", "Ivanov");

        // Создаём тестовые вопросы
        questions = List.of(
                createQuestion("Question 1",
                        List.of(
                                new Answer("Answer 1.1", false),
                                new Answer("Answer 1.2", true),
                                new Answer("Answer 1.3", false)
                        )
                ),
                createQuestion("Question 2",
                        List.of(
                                new Answer("Answer 2.1", true),
                                new Answer("Answer 2.2", false),
                                new Answer("Answer 2.3", true)
                        )
                )
        );
    }

    private Question createQuestion(String text, List<Answer> answers) {
        return new Question(text, answers);
    }

    @Test
    void executeTestFor_ShouldDisplayQuestionsAndProcessAnswers() {
        // Given
        when(questionDao.findAll()).thenReturn(questions);

        // When
        TestResult result = testService.executeTestFor(student);

        // Then
        verify(ioService, times(1)).printFormattedLineLocalized("TestService.answer.the.questions");
        verify(ioService, times(8)).printFormattedLine(anyString(), anyInt(), anyString());
        verify(resultService, never()).showResult(result);
    }

    @Test
    void processUserAnswerWithRetries_ShouldReturnTrueForCorrectAnswer() {
        // Given
        Question question = questions.get(0);
        Set<Integer> correctIndices = Set.of(1); // Ответ 2 (индекс 1) — правильный

        when(ioService.readIntForRange(eq(1), eq(3), anyString())).thenReturn(2);

        // When
        boolean result = testService.processUserAnswerWithRetries(question, new TestResult(student));

        // Then
        assertTrue(result);
        verify(ioService).printLineLocalized("TestService.correct.answer");
    }

    @Test
    void processUserAnswerWithRetries_ShouldReturnFalseForIncorrectAnswer() {
        // Given
        Question question = questions.get(0);

        when(ioService.readIntForRange(eq(1), eq(3), anyString())).thenReturn(1);

        // When
        boolean result = testService.processUserAnswerWithRetries(question, new TestResult(student));

        // Then
        assertFalse(result);
        verify(ioService).printLineLocalized("TestService.incorrect.answer");
    }

    @Test
    void checkAnswerCorrectness_ShouldReturnTrueWhenAllCorrectAnswersChosen() {
        // Given
        Question question = questions.get(1); // У этого вопроса 2 правильных ответа
        Set<Integer> chosenIndices = Set.of(0, 2); // Выбраны ответы 1 и 3 (индексы 0 и 2)

        // When & Then
        assertTrue(testService.checkAnswerCorrectness(chosenIndices, question.answers()));
    }

    @Test
    void checkAnswerCorrectness_ShouldReturnFalseWhenWrongAnswerChosen() {
        // Given
        Question question = questions.get(0);
        Set<Integer> chosenIndices = Set.of(0); // Выбран неправильный ответ (индекс 0)

        // When & Then
        assertFalse(testService.checkAnswerCorrectness(chosenIndices, question.answers()));
    }

    @Test
    void parseUserInput_ShouldConvertToIndicesCorrectly() {
        // Given
        String input = "1, 2, 3";
        int totalAnswers = 3;

        // When
        Set<Integer> result = testService.parseUserInput(input, totalAnswers);

        // Then
        assertEquals(Set.of(0, 1, 2), result);
    }

    @Test
    void displayQuestion_ShouldShowNoAnswersMessageWhenEmpty() {
        // Given
        Question questionWithNoAnswers = new Question("Question with no answers", List.of());

        // When
        testService.displayQuestion(questionWithNoAnswers, 1);

        // Then
        verify(ioService).printLineLocalized("TestService.no.answers.available");
    }

    @Test
    void executeTestFor_ShouldHandleEmptyQuestionsList() {
        // Given
        when(questionDao.findAll()).thenReturn(List.of());

        // When
        TestResult result = testService.executeTestFor(student);

        // Then
        assertNotNull(result);
        assertEquals(student, result.getStudent());
        assertEquals(0, result.getRightAnswersCount());
    }

    @DisplayName(" должен возвращать приветствие для всех форм команды логина")
    @Test
    void shouldReturnExpectedGreetingAfterLoginCommandEvaluated() throws Exception {
        when(inputProvider.readInput())
                .thenReturn(() -> COMMAND_LOGIN)
                .thenReturn(() -> COMMAND_LOGIN_SHORT)
                .thenReturn(() -> String.format(COMMAND_LOGIN_PATTERN, COMMAND_LOGIN_SHORT, CUSTOM_LOGIN))
                .thenReturn(null);

        shell.run(inputProvider);
        verify(resultHandlerService, times(3)).handle(argumentCaptor.capture());
        List<Object> results = argumentCaptor.getAllValues();
        assertThat(results).containsExactlyInAnyOrder(String.format(GREETING_PATTERN, DEFAULT_LOGIN),
                String.format(GREETING_PATTERN, DEFAULT_LOGIN),
                String.format(GREETING_PATTERN, CUSTOM_LOGIN));
    }
}
