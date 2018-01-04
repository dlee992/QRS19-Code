package programEntry;

import experiment.StatisticsForAll;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static programEntry.GP.*;
import static programEntry.MainClass.createAndShowGUI;

public class TestDataSet {

    private static String fileSeparator = System.getProperty("file.separator");
    private static int indexOfTesting = 10;
    public static int upperLimit = Integer.MAX_VALUE;
    public static int fileSizeLimit = 500;
    public static int timeout = 2; //单位是 TimeUnit.MINUTES.

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
    private static void testEUESE() throws IOException, InterruptedException {
        String inDirPath = parent_dir + fileSeparator + "Inputs" + fileSeparator + "EUSES-Corpus";
        File inputDir = new File(inDirPath);


        List<Future<?>> futureList = new ArrayList<>();
        Map<Future<?>, String> errorExcelMap = new HashMap<>();
        List<String> errorExcelList = new ArrayList<>();

        int i = 0;
        for (File subDir:
             inputDir.listFiles()) {

            if (subDir.isFile()) continue;
            if (!(i==7)) {
                i++;
                continue;
            }
            i++;

            staAll = new StatisticsForAll();
            staAll.setBeginTime(System.currentTimeMillis());
            futureList.clear();
            errorExcelMap.clear();
            errorExcelList.clear();

            System.out.println(subDir.getName());

            File processedDir = new File(subDir.getAbsolutePath() + fileSeparator + "processed");
            for (File excelFile:
                 processedDir.listFiles()) {
                if (!excelFile.getName().toLowerCase().endsWith("xls")) {
                    excelFile.delete();
                    continue;
                }

//                if (!excelFile.getName().startsWith("2004_PUBLIC_BUGS_INVENTORY")) continue;
//                double size = excelFile.length()/1024.0;
//                if (size >= fileSizeLimit) continue;
                //System.out.println(excelFile.getName());

                //直接处理这个Excel文件
                Runnable runnable = () -> {
                    String excelName = excelFile.getName();
                    try {
                        MainClass.testSpreadsheet(excelFile, staAll, logBuffer, index, true, subDir.getName());
                    } catch (Exception|OutOfMemoryError e) {
                        e.printStackTrace();
                        //TODO:这里的异常SS怎么获取
                        errorExcelList.add(excelName);
                    }
                };

                Future<?> task = exeService.submit(runnable);
                futureList.add(task);
                errorExcelMap.put(task, excelFile.getName());
            }

            for (Future<?> task:
                    futureList) {
                String errorExcelName = errorExcelMap.get(task);

                try {
                    task.get(5, TimeUnit.MINUTES);
                }
                catch (ExecutionException e) {
                    System.out.println("TestDataSet @71 line: Execution Exception.");
                }
                catch (TimeoutException e) {
                    System.out.println("TestDataSet @71 line: Timeout E-xception, and excel name = " + errorExcelName);
                    errorExcelList.add(errorExcelName);
                }
                finally {
                    task.cancel(true);
                    System.out.println("Try to cancel the timeout task.");
                }
            }

            MainClass.executorDone(exeService, staAll, prefixOutDir, logBuffer, errorExcelList);
        }

        System.out.println("everything is done.");
        createAndShowGUI();
    }


    public static void testEnron() {


    }
}
