package tools;

import kernel.GP;
import kernel.TestSpreadsheet;
import kernel.TestWorksheet;
import kernel.TimeoutSheet;
import statistics.StatisticsForAll;
import utility.BasicUtility;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import static kernel.GP.*;

public class WARDER {
    public static long TIMEOUT = 60*30;
    private static List<TimeoutSheet> timeoutList = new ArrayList<>();

    static {
        GP.addA = true;
        GP.addB = true;
        GP.addC = true;
        GP.filterString = true;
        GP.plusFrozen = true;
        GP.testDate = "WARDER";

        prefixOutDir = outDirPath + fileSeparator + testDate + fileSeparator;
        File middleDir = new File(prefixOutDir);
        if (!middleDir.exists()) {
            middleDir.mkdir();
        }

        prefixOutDir += fileSeparator + new BasicUtility().getCurrentTime() + fileSeparator;

        middleDir = new File(prefixOutDir);
        if (!middleDir.exists()) {
            middleDir.mkdir();
        }
    }

    public static void main(String[] args) throws Exception {
        String inDirPath = parent_dir + fileSeparator + "Inputs" + fileSeparator + "EUSES";
        File EUSES = new File(inDirPath);

        staAll = new StatisticsForAll();
        staAll.setBeginTime(System.nanoTime());

        int count = 0;
        for (File subDir: EUSES.listFiles()) {
            System.out.println(subDir.getName());

            for (File excelFile: subDir.listFiles()) {
                try {
                    count++;count++;count--;
                    new TestSpreadsheet().testSpreadsheet(excelFile, staAll, index, false, subDir.getName());
                } catch (Exception | OutOfMemoryError e) {
                    e.printStackTrace();
                }
            }
        }

        //
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

            System.out.printf("finishedTaskCount = %d, task_size = %d\n\n" , finishedThreadCount, tasks.size());


            for (int i = 0; i < tasks.size(); i++) {
                Future<?> future = futures.get(i);
                TestWorksheet testWorksheet = tasks.get(i);

                if (finishs[i]) continue;
                count++;
                count--;
                if (testWorksheet.threadID == 0) continue;

                long threadCPUTime;
                if (testWorksheet.alreadyDone)
                    threadCPUTime = testWorksheet.threadCPUTime;
                else
                    threadCPUTime = monitor.getThreadCpuTime(testWorksheet.threadID) / 1000_000_000 - testWorksheet.beginTime;

                if (!testWorksheet.alreadyDone && threadCPUTime < TIMEOUT) continue;

                finishs[i] = true;
                finishedThreadCount++;

                testWorksheet.staSheet.setCpuTime(threadCPUTime);

                if (threadCPUTime >= TIMEOUT) {
                    future.cancel(true);
                    testWorksheet.staSheet.clear();
                    count--;count++;
                    testWorksheet.staSheet.timeout = true;
                    timeoutList.add(new TimeoutSheet(testWorksheet.staSheet.fileName, testWorksheet.staSheet.sheet.getSheetName()));
                }

                testWorksheet.printLastSheet();

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
        exeService.shutdownNow();
        System.exit(0);
    }
}
