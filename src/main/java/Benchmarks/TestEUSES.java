package Benchmarks;

import experiment.StatisticsForAll;
import org.apache.poi.ss.usermodel.Sheet;
import programEntry.TestWorksheet;
import programEntry.TimeoutSheet;

import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static programEntry.GP.*;
import static programEntry.TestSpreadsheet.testSpreadsheet;

public class TestEUSES {

    private static String fileSeparator = System.getProperty("file.separator");
//    private static int MAXFILES = 101;//Integer.MAX_VALUE;
    private static int MAXFILES = Integer.MAX_VALUE;
    public static long TIMEOUT = 60*5; //以秒为单位
    private static Set<String> testTarget = new HashSet<>();
    private static List<TimeoutSheet> timeoutList = new ArrayList<>();
    private static String[] categories = {"cs101", "filby", "forms3", "jackson", "personal",
            "database", "financial", "grades", "homework", "inventory", "modeling"};


    public static void main(String[] args) throws IOException {
        PrintStream file_stream = new PrintStream("debug_log.txt");
        System.setOut(file_stream);

        testEUESE();
        System.exit(0);
//        testEnron();
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
    private static void testEUESE() throws IOException {

        int lower_bound = 2;
        int upper_bound = 3;
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
            if (count > MAXFILES) continue;
            if (!testTarget.contains(subDir.getName())) continue;
            System.out.println(subDir.getName());

            File processedDir = new File(subDir.getAbsolutePath() + fileSeparator + "processed");
            for (File excelFile:
                 processedDir.listFiles()) {
                //直接处理这个Excel文件
//                if (!excelFile.getName().equals("spreadsheets5.xls")) continue;
                try {
                    count++;
                    if (count > MAXFILES) break;
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
        timeoutMonitor(TIMEOUT);
    }

    public static void timeoutMonitor(long TIMEOUT) throws IOException {

        Iterator<TestWorksheet> taskIter = tasks.iterator();
        for (Future<?> future:
                futures) {
            TestWorksheet testWorksheet = taskIter.next();

            try {
//                System.out.println("[" + Thread.currentThread().getName() + "]: MainThread 2");
                long timeout = (long) (TIMEOUT * 1_000_000_000.0); //5 seconds
                while (testWorksheet.beginTime == -1) {
                    Thread.sleep(5000);
                }

                timeout = timeout - (System.nanoTime() - testWorksheet.beginTime);
                if (timeout < 0)
                    timeout = 0;

                //TODO: 这里又有一个bug，如果timeout为负数，那么表示该任务已经超时 或者 早已经执行结束，这里需要额外判断和处理
                System.out.println("Timeout left : " + timeout/1000_000_000
                        + " Seconds --- SS: " +  testWorksheet.staSheet.fileName +
                        ", WS: " + testWorksheet.staSheet.sheet.getSheetName());

                future.get(timeout, TimeUnit.NANOSECONDS);

                long consumedTime = (testWorksheet.staSheet.getEndTime() - testWorksheet.staSheet.getBeginTime())
                        /1000_000_000;
                Sheet sheet = testWorksheet.staSheet.sheet;
                if (consumedTime >= 300) {
                    timeoutList.add(new TimeoutSheet(testWorksheet.staSheet.fileName, sheet.getSheetName()));
                    testWorksheet.staSheet.clear();
                }
                staAll.add(testWorksheet.staSheet, logBuffer);
//                System.out.println("staAll size = " + staAll.sheetList.size());

            } catch (TimeoutException ignored) {
                System.out.println("Timeout in : " + (System.nanoTime() - testWorksheet.beginTime)/ 1_000_000_000.0
                        + " Seconds --- SS: " +  testWorksheet.staSheet.fileName +
                        ", WS: " + testWorksheet.staSheet.sheet.getSheetName());

                testWorksheet.staSheet.clear();
                testWorksheet.staSheet.setEndTime(System.nanoTime());
                testWorksheet.printLastSheet();

                staAll.add(testWorksheet.staSheet, logBuffer);
//                System.out.println("staAll size = " + staAll.sheetList.size());

                Sheet sheet = testWorksheet.staSheet.sheet;
                timeoutList.add(new TimeoutSheet(testWorksheet.staSheet.fileName, sheet.getSheetName()));

                future.cancel(true);
            }
            catch (ExecutionException | InterruptedException | IllegalStateException ignored) {
                System.out.println("What to do?");
            } finally {

            }
        }

        System.out.println("staAll size = " + staAll.sheetList.size());
        staAll.log(prefixOutDir, null);

//        try {
//            GoogleMail.Send("njulida", "lida2016", "354052374@qq.com",
//                    "实验结束", "oh-ha.");
//        } catch (MessagingException e) {
//            e.printStackTrace();
//        }

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

        timeoutMonitor(TIMEOUT);
    }

    private static void readDuplicates() {
    }
}
