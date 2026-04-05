package otus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import ru.otus.configHw.config.AppConfiguration;
import ru.otus.configHw.dao.CsvQuestionDao;
import ru.otus.configHw.dao.QuestionDao;
import ru.otus.configHw.domain.Answer;
import ru.otus.configHw.domain.Question;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CsvQuestionDaoTest {

    private QuestionDao questionDao;
    private List<Question> questionList;
    private Question question;
    private static String ANSWER1 = "Melbourne";
    private static String ANSWER2 = "Canberra";
    private static String ANSWER3 = "Sydney";
    private static String QUESTION_1 = "What is the capital of Australia?";

    @BeforeEach
    void setUp() throws IOException {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfiguration.class);
        questionDao = context.getBean(CsvQuestionDao.class);
    }

    private String removeBom(String input) {
        return input.replace("\uFEFF", "").trim();
    }

    @Test
    void shouldReadQuestionsFromCsvFile1() {
        List<Question> questions = questionDao.findAll();

        assertThat(questions).hasSize(5);
        Question firstQuestion = questions.get(0);

        // Очищаем обе строки от BOM и сравниваем
        assertThat(removeBom(firstQuestion.text()))
                .isEqualTo(removeBom(QUESTION_1));

        assertThat(removeBom(firstQuestion.answers().get(0).text()))
                .isEqualTo(removeBom(ANSWER1));
        assertThat(firstQuestion.answers().get(0).isCorrect()).isFalse();

        assertThat(removeBom(firstQuestion.answers().get(1).text()))
                .isEqualTo(removeBom(ANSWER2));
        assertThat(firstQuestion.answers().get(1).isCorrect()).isTrue();

        assertThat(removeBom(firstQuestion.answers().get(2).text()))
                .isEqualTo(removeBom(ANSWER3));
        assertThat(firstQuestion.answers().get(2).isCorrect()).isFalse();
    }
}
