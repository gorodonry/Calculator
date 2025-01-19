import java.util.List;
import java.util.Scanner;
import java.util.stream.Stream;

/**
 * Contains various methods for asking for basic input from users.
 */
public final class BasicInput {
    public static List<String> positiveAnswers = List.of("y", "yes");
    public static List<String> validAnswers = Stream.concat(positiveAnswers.stream(), Stream.of("n", "no")).toList();

    /**
     * Asks a binary question to the user and returns a boolean indicating their response.
     *
     * @param prompt the prompt for the question.
     * @param systemInputStreamReader a scanner linked to {@link System#in} to ask the question with.
     *
     * @return true if they responded with yes, false if they responded with no.
     */
    public static boolean askBinaryQuestion(String prompt, Scanner systemInputStreamReader) {
        String answer = "";

        while (validAnswers.stream().noneMatch(answer::equals)) {
            System.out.print(prompt);
            answer = systemInputStreamReader.nextLine().trim().toLowerCase();

            if (validAnswers.stream().noneMatch(answer::equals)) {
                System.out.println("Pls enter either 'y' or 'n' :)");
            }
        }

        return positiveAnswers.contains(answer);
    }
}
