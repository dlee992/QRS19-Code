import featureExtraction.weakFeatureExtraction.Wrapper;
import org.apache.logging.log4j.core.util.ExecutorServices;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import utility.BasicUtility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Yolanda on 2017/5/8.
 */
public class TestCACheck {
    private static String fileSeparator = System.getProperty("file.separator");
    private static String parent_dir = System.getProperty("user.dir");
    private static String mode = "Default original spreadsheets";
    private static String inDirPath = parent_dir + fileSeparator + "Inputs" + fileSeparator + mode;
    private static String currentTime = new BasicUtility().getCurrentTime();
    private static String outDirPath = parent_dir + fileSeparator + "Outputs" + fileSeparator + "CACheck_" + currentTime;
    private static int upperBound = 5;

    public static void mainx(String args[]) throws IOException, InvalidFormatException {

        File inDir = new File(inDirPath);
        File outDir = new File(outDirPath);
        if (!outDir.exists()) {

            final boolean mkdir = outDir.mkdir();
        }

        AtomicInteger index = new AtomicInteger(0);

        ExecutorService executorService = Executors.newCachedThreadPool();

        for (File file : inDir.listFiles()) {
            //if (index == upperBound) break;

            if (!file.getName().endsWith("xls")) continue;
            executorService.execute(() -> {
                try {

                    System.out.println("index = " + index.incrementAndGet() + " ######## Begin in: " +
                            inDir.getName() + "/" + file.getName() + "'########");

                    Workbook workbook = WorkbookFactory.create(new FileInputStream(file));

                    for (Sheet sheet: workbook) {

                        Wrapper singletonWrapper = new Wrapper(6);
                        singletonWrapper.setSheet(sheet);
                        singletonWrapper.processSheet();
                    }

                    File outFile = new File(outDirPath + fileSeparator + file.getName());
                    FileOutputStream outFileStream = new FileOutputStream(outFile);
                    workbook.write(outFileStream);
                    outFileStream.close();
                    workbook.close();

                    System.out.println("index = " + index + " ######## End in: " +
                            outDir.getName() + "/" + outFile.getName() + "'########");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });


        }
    }
}
