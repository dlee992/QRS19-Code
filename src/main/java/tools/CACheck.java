package tools;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import java.io.IOException;

/**
 * Created by Yolanda on 2017/5/8.
 */
public class CACheck {
    public static String toolName = "CACheck";
    public static String dataset = "VEnron2-Clean";
    public static boolean checking = false;
    public static int stepIndex = 2;
    public static int restart = 188;

    public static void main(String args[]) throws IOException, InvalidFormatException {
        if (checking) {
            UCheck.computeDetectionResult(toolName);
            return;
        }
        AmCheck.runTests(toolName, 6, stepIndex);
    }
}
