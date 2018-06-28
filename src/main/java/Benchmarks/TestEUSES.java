package Benchmarks;

import experiment.StatisticsForAll;
import org.apache.poi.ss.usermodel.Sheet;
import programEntry.TestWorksheet;
import programEntry.TimeoutSheet;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.*;
import java.util.concurrent.Future;

import static programEntry.GP.*;
import static programEntry.TestSpreadsheet.testSpreadsheet;

public class TestEUSES {

    private static String fileSeparator = System.getProperty("file.separator");
//    private static int MAXFILES = 101;//Integer.MAX_VALUE;
    private static int MAXFILES = Integer.MAX_VALUE;
    public static long TIMEOUT = 60*5; //以秒为单位
    private static Set<String> testTarget = new HashSet<>();
    private static List<TimeoutSheet> timeoutList = new ArrayList<>();
    private static String[] categories = {"cs101", "filby", "forms3", "jackson", "personal", //0--4
            "database", "financial", "grades", "homework", "inventory", "modeling"}; //5--10

    public static void main(String[] args) throws IOException {
        PrintStream file_stream = new PrintStream("debug_log.txt");
        System.setOut(file_stream);

        testEUESE();
        System.exit(0);
//        testEnron();
    }


    /*
     1:timeout要针对每张worksheet来设定,对象不能是spreadsheet.
     2:统计更多的输出信息,包括每张表有 #cells, #formula cells, #data cells,
        #clusters, #cells in clusters, #defective cells,及其相关百分比,具体哪些需要计算还是要参考CACheck,毕竟是已有蓝本.
     3:与程序本身无关,对Enron数据集取其版本化过滤之后的子集来运行,甚至可以考虑是否使用FUSE-Corpus.
     4:需要确定哪些worksheet需要被列入到最终的统计栏中,
        出于保守考虑,应该全记录下来,也方便后续统计每张SS包含#worksheet,含有公式的#worksheet.
     5:暂时还没有想到第四点.
    */
    private static void testEUESE() throws IOException {

        int lower_bound = 6;
        int upper_bound = lower_bound+1;

        //range: [file_lower_bound, file_upper_bound - 1]
        int file_lower_bound = 001;
        int file_upper_bound = file_lower_bound + 720;

        for (int i = lower_bound; i < upper_bound; i++) {
            String category_i = categories[i];
            testTarget.add(category_i);
        }

        String inDirPath = parent_dir + fileSeparator + "Inputs" + fileSeparator + "EUSES-Corpus";
        File inputDir = new File(inDirPath);

        staAll = new StatisticsForAll();
        staAll.setBeginTime(System.nanoTime());

        int count = 0;
        for (File subDir:
             inputDir.listFiles()) {
            if (subDir.isFile()) continue;
            if (!testTarget.contains(subDir.getName())) continue;

            System.out.println(subDir.getName());

            File processedDir = new File(subDir.getAbsolutePath() + fileSeparator + "processed");
            for (File excelFile:
                 processedDir.listFiles()) {
                //直接处理这个Excel文件
//                if (!excelFile.getName().equals("spreadsheets5.xls")) continue;
                try {
                    if (file_lower_bound > count || file_upper_bound <= count) {
                        count++;
                        continue;
                    }
                    count++;
                    testSpreadsheet(excelFile, staAll, logBuffer, index, true, subDir.getName());
                    if (count % 100 == 0) staAll.log(prefixOutDir,  null);
                } catch (Exception  | OutOfMemoryError e) {
                    e.printStackTrace();
                }
            }
        }

        //执行所有任务
        //对所有Callable的return value做相应处理

        //对所有Callable的return value做相应处理
        timeoutMonitor();
    }

    public static void timeoutMonitor() throws IOException {

        ThreadMXBean monitor = ManagementFactory.getThreadMXBean();

        int finishedThreadCount = 0;

        finishs = new Boolean[tasks.size()];
        for (int i=0; i < tasks.size(); i++) finishs[i] = false;


        while (finishedThreadCount < tasks.size()) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.err.printf("finishedThreadCount = %d, task_size = %d\n\n" , finishedThreadCount, tasks.size() );


            for (int i = 0; i < tasks.size(); i++) {
                Future<?> future = futures.get(i);
                TestWorksheet testWorksheet = tasks.get(i);

                if (finishs[i]) continue;

                if (testWorksheet.threadID == 0) continue;

                long executedTime;
                if (testWorksheet.alreadyDone)
                    executedTime = testWorksheet.threadCPUTime;
                else
                    executedTime = monitor.getThreadCpuTime(testWorksheet.threadID) / 1000_000_000;

                if (!testWorksheet.alreadyDone && executedTime < TIMEOUT) continue;

                finishs[i] = true;
                finishedThreadCount++;

                if (executedTime >= TIMEOUT) {
                        future.cancel(true);
                        Sheet sheet = testWorksheet.staSheet.sheet;
                        timeoutList.add(new TimeoutSheet(testWorksheet.staSheet.fileName, sheet.getSheetName()));
                        testWorksheet.staSheet.clear();
                }

                testWorksheet.staSheet.setCPUTime(executedTime);
                staAll.add(testWorksheet.staSheet, logBuffer);
            }
        }

        System.out.println("staAll size = " + staAll.sheetList.size());
        staAll.log(prefixOutDir, null);

        BufferedWriter bufferedWriter = new BufferedWriter(
                new FileWriter(prefixOutDir + "timeoutList_" + timeoutList.size() + ".txt"));
        for (TimeoutSheet timeoutSheet:
             timeoutList) {
            bufferedWriter.write(timeoutSheet.spreadsheetName + " " + timeoutSheet.sheetName);
            bufferedWriter.newLine();
        }

        bufferedWriter.close();

        staAll.getInfo();

        return;
//        exeService.shutdownNow();
//        createAndShowGUI();
//        System.exit(0);
    }

    private static void testEnron() throws IOException {
        readDuplicates();

        String inDirPath = parent_dir + fileSeparator + "Inputs" + fileSeparator + "Enron-Corpus";
        File inputDir = new File(inDirPath);

        staAll = new StatisticsForAll();
        staAll.setBeginTime(System.nanoTime());

        int count = 0;
        for (File ssfile :
                inputDir.listFiles()) {
            try {
                count++;
                if (count > MAXFILES) break;
                testSpreadsheet(ssfile, staAll, logBuffer, index, true, "Enron");
                if (count % 100 == 0) staAll.log(prefixOutDir,  null);
            } catch (Exception  | OutOfMemoryError e) {
                e.printStackTrace();
            }
        }

        timeoutMonitor();
    }

    private static void readDuplicates() {
    }
}
