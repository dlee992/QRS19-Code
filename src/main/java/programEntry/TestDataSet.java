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
import static programEntry.TestSpreadsheet.testSpreadsheet;

public class TestDataSet {

    private static String fileSeparator = System.getProperty("file.separator");
    private static int indexOfTesting = 10;
    static int upperLimit = Integer.MAX_VALUE;
    protected static int timeout = 5; //单位是 TimeUnit.MINUTES.

    public static void main(String[] args) {
        try {
            testEUESE();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /*TODO:
     1:timeout要针对每张worksheet来设定,对象不能是spreadsheet.
     2:统计更多的输出信息,包括每张表有 #cells, #formula cells, #data cells,
        #clusters, #cells in clusters, #defective cells,及其相关百分比,具体哪些需要计算还是要参考CACheck,毕竟是已有蓝本.
     3:与程序本身无关,对Enron数据集取其版本化过滤之后的子集来运行,甚至可以考虑是否使用FUSE-Corpus.
     4:需要确定哪些worksheet需要被列入到最终的统计栏中,
        出于保守考虑,应该全记录下来,也方便后续统计每张SS包含#worksheet,含有公式的#worksheet.
     5:暂时还没有想到第四点.
    */
    private static void testEUESE() throws InterruptedException, IOException {
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
                        testSpreadsheet(excelFile, staAll, logBuffer, index, true, subDir.getName());
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
                    task.get(timeout, TimeUnit.MINUTES);
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
