package programEntry;

import java.io.File;
import java.io.IOException;

import static programEntry.GP.*;

public class TestDataSet {

    private static String fileSeparator = System.getProperty("file.separator");
    private static int indexOfTesting = 5;

    public static void main(String[] args) {
        try {
            testEUESE();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }


    /*TODO:
     1-针对目录结构筛选出需要进行运算的Excel表格
     2-因为没有ground truths,在ground truths部分是否需要额外的处理
     3-just run it
    */
    public static void testEUESE() throws IOException, InterruptedException {
        String inDirPath = parent_dir + fileSeparator + "Inputs" + fileSeparator + "EUSES-Corpus";
        File inputDir = new File(inDirPath);

        int i = 0;
        for (File subDir:
             inputDir.listFiles()) {

            if (subDir.isFile()) continue;
            if (i++ != indexOfTesting) continue;
            System.out.println(subDir.getName());

            File processedDir = new File(subDir.getAbsolutePath() + fileSeparator + "processed");
            for (File excelFile:
                 processedDir.listFiles()) {
                if (!excelFile.getName().toLowerCase().endsWith("xls")) continue;

                System.out.println(excelFile.getName());
                //直接处理这个Excel文件
                exeService.execute(() -> {
                    try {
                        MainClass.testSpreadsheet(excelFile, staAll, logBuffer, index, true, subDir.getName());
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        }

        MainClass.executorDone(exeService, staAll, prefixOutDir, logBuffer);
    }


    public static void testEnron() {


    }
}
