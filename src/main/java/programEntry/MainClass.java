package programEntry;

import ThirdParty.CACheck.amcheck.AnalysisPattern;
import experiment.StatisticsForAll;
import experiment.StatisticsForSheet;

import javax.swing.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static programEntry.GP.*;
import static programEntry.TestSpreadsheet.testSpreadsheet;

/**
 * Created by lida on 2017/6/27.
 */
public class MainClass {

    static String groundTruthPath;
    private static String mode;
    private static String inDirPath;
    private static String programState;

    private static long TIMEOUT = 60; //seconds

    static AtomicInteger numberOfFormula = new AtomicInteger(0);
    static ArrayList<String> ssNameList = new ArrayList<>();
    private static AnalysisPattern analysisPattern = new AnalysisPattern();


    public static void main(String[] args) throws Exception {
        inDirPath  = parent_dir + fileSeparator + "Inputs";

        if (args.length==0) {
            args = new String[1];
            args[0] = "null";
        }

        programState = "Debugging";
        commandHandling(args);

        File inDir = new File(inDirPath);
        FilenameFilter filter1 = (directory, fileName) -> fileName.toLowerCase().endsWith(".xls") ||
                fileName.toLowerCase().endsWith(".xlsx");
        FileFilter filter2 = file -> (!file.isHidden() && file.isDirectory() && file.getName().equals(mode));
        File[] categories = inDir.listFiles(filter2);

        staAll = new StatisticsForAll();
        staAll.setBeginTime(System.nanoTime());

        //把任务添加到thread pool中,最后invokeAll执行
        for (int i = 0; categories != null && i < categories.length; i++) {
            File perCategory = new File(categories[i].getAbsolutePath());
            File[] files = perCategory.listFiles(filter1);

            assert files != null;

            int count = 0;
            for (File eachFile : files) {
//                if (!eachFile.getName().startsWith("0000")) continue;
//                if (!eachFile.getName().startsWith("act")) continue;
//                count ++;
//                if (count > 10) break;

                final File finalEachFile = new File(eachFile.getAbsolutePath());

                try {
                    testSpreadsheet(finalEachFile, staAll, logBuffer, index, false, perCategory.getName());
                }
                catch (Exception | OutOfMemoryError e) {
                    e.printStackTrace();
                }
            }
        }

        //执行所有任务
        //对所有Callable的return value做相应处理
        TestDataSet.timeoutMonitor(TIMEOUT);
    }


    public static void createAndShowGUI() {
        JFrame frame = new JFrame("程序结束");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JLabel label = new JLabel("程序结束");
        frame.getContentPane().add(label);

        frame.pack();
        frame.setVisible(true);
    }


    private static void commandHandling(String[] args) throws Exception {
        switch (args[0]) {
            case "-default":
                /*|| programState.equals("developing on default")*/

                groundTruthPath = inDirPath + fileSeparator + "Default marked worksheets (smelly cells & clusters)";
                mode = "Default original spreadsheets";

                if (args.length == 1) return;

//                GP.O2_1 = args[1].equals("true");
//                GP.O2_2 = args[2].equals("true");
//                GP.O3 = args[3].equals("true");
//                GP.logicalOperator = args[4].equals("true");

//                GP.parameter_1 = Double.parseDouble(args[5]);
//                GP.parameter_2 = 2 * GP.parameter_1;
//                GP.parameter_3 = Double.parseDouble(args[6]);
                break;
            case "-custom":
                /*|| programState.equals("developing on custom")*/

                groundTruthPath = inDirPath + fileSeparator + "User-chosen marked worksheets";
                mode = "User-chosen original spreadsheets";
                break;
            default:
                if (programState.equals("Debugging")) {
                    groundTruthPath = inDirPath + fileSeparator + "Default marked worksheets (smelly cells & clusters)";
                    mode = "Default original spreadsheets";
                }
                else {
                    throw new Exception();
                }
        }
    }
}
