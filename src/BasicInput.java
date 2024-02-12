import java.util.Arrays;
import java.util.Scanner;

public abstract class BasicInput {
    public static String[] POSITIVE_ANSWERS = { "y", "yes" };
    public static String[] VALID_ANSWERS = { "y", "yes", "n", "no" };

    public static boolean askBinaryQuestion(String prompt, Scanner systemInputStreamReader) {
        String answer = "";

        while (Arrays.stream(VALID_ANSWERS).noneMatch(answer::equals)) {
            System.out.print(prompt);
            answer = systemInputStreamReader.nextLine().trim().toLowerCase();

            if (Arrays.stream(VALID_ANSWERS).noneMatch(answer::equals)) {
                System.out.println("Pls enter either 'y' or 'n' :)");
            }
        }

        return Arrays.asList(POSITIVE_ANSWERS).contains(answer);
    }
}
