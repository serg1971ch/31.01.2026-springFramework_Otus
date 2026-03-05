package ru.otus.service;
import lombok.Data;
import ru.otus.domain.Message;
import java.util.regex.Pattern;

@Data
public class MessageServiceImpl implements MessageService {
    private static final Pattern NAME_PATTERN =
            Pattern.compile("^[a-zA-Z][a-zA-Z\\s-]*$");

    private final IOService ioService;
    private Message message = new Message();

    @Override
    public String getNameForMessage() {
        if (message.getFromFirstName() != null && message.getFromLastName() != null) {
            return message.getFromFirstName() + " " + message.getFromLastName();
        }

        String firstName;
        do {
            ioService.printLine("Write your first name:");
            firstName = ioService.readLine().trim();
            if (!isValidName(firstName)) {
                ioService.printLine("First name is invalid. Please use letters, spaces, or hyphens.");
            }
        } while (!isValidName(firstName));

        message.setFromFirstName(firstName);

        String lastName;
        do {
            ioService.printLine("Write your last name:");
            lastName = ioService.readLine().trim();
            if (!isValidName(lastName)) {
                ioService.printLine("Last name is invalid. Please use letters, spaces, or hyphens.");
            }
        } while (!isValidName(lastName));

        message.setFromLastName(lastName);
        return firstName + " " + lastName;
    }

    @Override
    public Message getMessage(int errCount) {
        String fullName = getNameForMessage();
        String bodyMessage = String.format(
                """
                Dear %s,
                You had reached: %d correct answers.
                And %d incorrect answers.
                Test finished. Thank you!""",
                fullName, 5 - errCount, errCount
        );
        message.setMessage(bodyMessage);
        return message;
    }

    public static boolean isValidName(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        return NAME_PATTERN.matcher(name).matches();
    }
}
