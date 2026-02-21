package otus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.otus.dao.CsvQuestionDao;
import ru.otus.domain.Answer;
import ru.otus.domain.Question;
import ru.otus.service.IOService;
import ru.otus.service.MessageService;
import ru.otus.service.TestServiceImpl;

import java.util.List;

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
    void executeTest_basicFlow_shouldWork() {
        when(messageService.getNameForMessage()).thenReturn("John");
        when(questionDao.findAll()).thenReturn(questions);
        testService.executeTest();

        verify(ioService).printFormattedLine("Hello, %s!", "John");
        verify(ioService).printFormattedLine("Please answer the questions below%n");
        verify(ioService).printFormattedLine("%d. %s", 0, "What is capital of France?");
        verify(ioService).printFormattedLine(" %d. %s", 1, "Paris");
        verify(ioService).printFormattedLine(" %d. %s", 2, "London");
        verify(ioService).printFormattedLine(" %d. %s", 3, "Berlin");
        verify(ioService).printFormattedLine("%n");
    }
}
