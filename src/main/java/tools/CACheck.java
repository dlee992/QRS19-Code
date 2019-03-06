package tools;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import java.io.IOException;

/**
 * Created by Yolanda on 2017/5/8.
 */
public class CACheck {
    public static String toolName = "CACheck";
    public static boolean checking = true;

    public static void main(String args[]) throws IOException, InvalidFormatException {
        if (checking) {
            UCheck.computeDetectionResult(toolName);
            return;
        }
        AmCheck.runTests(toolName, 6);
    }
}
