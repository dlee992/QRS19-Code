package kernel;

import statistics.StatisticsForAll;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import static kernel.GP.*;

public class MainClass {
    public static long TIMEOUT = 60*5;
    private static List<TimeoutSheet> timeoutList = new ArrayList<>();
    public static String dataset = "VEnron2-Clean";

    static {
        /*
        in order:
        single-cell validity
        multi-cell validity
        cluster validity
         */
        GP.addC = GP.filterString = GP.plusFrozen = true;
        GP.addB = true;
        GP.addA = true;
        GP.testDate = "WARDER";
        buildArtifact = true;

        prefixOutDir = outDirPath  + fileSeparator;
        File middleDir = new File(prefixOutDir);
        if (!middleDir.exists()) {
            middleDir.mkdir();
        }

        middleDir = new File(prefixOutDir);
        if (!middleDir.exists()) {
            middleDir.mkdir();
        }
    }

    public static void main(String[] args) throws Exception {

        System.out.println("#####################################################################\n"+
                           "                Please move your testing spreadsheets \n" +
                           "                into the sub-directory \"Inputs\".\n" +
                           "#####################################################################\n");

        String inputDirName = parent_dir + fileSeparator + "Inputs";
        File inputDir = new File(inputDirName);

        staAll = new StatisticsForAll();
        staAll.setBeginTime(System.nanoTime());

        for (File excelFile: inputDir.listFiles()) {
            try {
                new TestSpreadsheet().testSpreadsheet(excelFile, staAll, index, false, inputDir.getName());
            } catch (Exception | OutOfMemoryError e) {
                e.printStackTrace();
            }
        }


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

//            System.out.printf("finishedSheetCount = %d / %d at %s\n\n" , finishedThreadCount, tasks.size(),
//                    new BasicUtility().getCurrentTime());


            for (int i = 0; i < tasks.size(); i++) {
                Future<?> future = futures.get(i);
                TestWorksheet testWorksheet = tasks.get(i);

                if (finishs[i]) continue;
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
                    //future.cancel(true);
                    testWorksheet.stop();
                    testWorksheet.staSheet.clear();
                    testWorksheet.staSheet.timeout = true;
                    timeoutList.add(new TimeoutSheet(testWorksheet.staSheet.fileName, testWorksheet.staSheet.sheet.getSheetName()));
                }

                testWorksheet.printLastSheet();

                staAll.add(testWorksheet.staSheet, logBuffer);
            }
        }

        //System.out.println("staAll size = " + staAll.sheetList.size());
        staAll.log(prefixOutDir, null);

        BufferedWriter bufferedWriter = new BufferedWriter(
                new FileWriter(prefixOutDir + "TimeoutSheetList" + ".log"));
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
