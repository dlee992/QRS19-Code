package experiment;

import ThirdParty.CACheck.AMSheet;
import ThirdParty.CACheck.CellArray;
import ThirdParty.CACheck.amcheck.AnalysisPattern;
import ThirdParty.CACheck.amcheck.ExcelAnalysis;
import ThirdParty.CACheck.cellarray.extract.CAResult;
import ThirdParty.CACheck.snippet.ExtractSnippet;
import ThirdParty.CACheck.snippet.Snippet;
import ThirdParty.CACheck.util.Log;
import ThirdParty.CACheck.util.Utils;
import clustering.smellDetectionClustering.Smell;
import entity.Cluster;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellReference;
import utility.BasicUtility;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
//import static programEntry.GP.O2_1;

/**
 * Created by Yolanda on 2017/6/14.
 */
public class CACheckEvaluation {

    private static String fileSeparator = System.getProperty("file.separator");

    private static String groundTruthPath;
    private static String outDirPath;
    private static String mode;
    private static String inDirPath;
    private static String programState;

    private static AtomicInteger numberOfFormula = new AtomicInteger(0);

    public static void main(String[] args) throws Exception {
//        System.out.println("java.library.path = " + System.getProperty("java.library.path"));

        String parent_dir = System.getProperty("user.dir");

        inDirPath  = parent_dir + fileSeparator + "Inputs";
        outDirPath = parent_dir + fileSeparator + "Outputs";
        String statisticsResult = outDirPath + fileSeparator + "Statistics_result.xlsx";


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

        final StatisticsForAll staAll = new StatisticsForAll();
        staAll.setBeginTime(System.currentTimeMillis());

        ExecutorService executorService = Executors.newCachedThreadPool();

        String logFile = outDirPath + fileSeparator + "logCACheck.txt";
        BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true));

        AnalysisPattern analysisPattern = new AnalysisPattern();
        analysisPattern.setType(6);

        for (int i = 0; categories != null && i < categories.length; i++) {
            File perCategory = new File(categories[i].getAbsolutePath());
            File[] files = perCategory.listFiles(filter1);

            AtomicInteger index = new AtomicInteger();
            assert files != null;
            for (File eachFile : files) {
                //if ( ! eachFile.getName().equals("1999%20PWR%20Effluent-DRAFT.xls")) continue;
                //if (index.get() == 10) break;

                final File finalEachFile = new File(eachFile.getAbsolutePath());
                System.out.println("index = " +(index.incrementAndGet())+ " ########Process '" +
                        categories[i].getName() + "/" + eachFile.getName() + "'########");

                //handleRequestForWorkbook(finalEachFile, staAll, writer, analysisPattern);
                //System.out.println("index = "+ index +" ######## End in: '" + eachFile.getName() + "'########");

                executorService.execute(() -> {
                    try {
                        handleRequestForWorkbook(finalEachFile, staAll, writer, analysisPattern);
                        System.out.println("index = "+ index +" ######## End in: '" + eachFile.getName() + "'########");
                    } catch (IOException | InvalidFormatException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });

            }
        }

        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.DAYS);

        staAll.setEndTime(System.currentTimeMillis());
        staAll.log(statisticsResult);

        //FIXME:
        System.out.println(statisticsResult);

        System.out.println("formula numbers : " + numberOfFormula);
    }

    private static void handleRequestForWorkbook(File eachFile, StatisticsForAll staAll, BufferedWriter writer, AnalysisPattern analysisPattern)
            throws IOException, InvalidFormatException, InterruptedException {

        String eachFileName = eachFile.getName();
        Workbook workbook = WorkbookFactory.create(new FileInputStream(eachFile));

        for (int j = 0; j < workbook.getNumberOfSheets(); j++) {
            Sheet sheet = workbook.getSheetAt(j);
            staAll.add(handleRequestForSheet(eachFileName, sheet, writer, analysisPattern));
        }

        String eachDirStr = outDirPath + fileSeparator + "ThirdParty/CACheck";

        File eachDir = new File(eachDirStr);
        if (!eachDir.exists()) {
            eachDir.mkdirs();
        }

        String suffix = null;
        if (workbook .getSpreadsheetVersion().name().equals("EXCEL97"))
            suffix = "xls";
        else if (workbook .getSpreadsheetVersion().name().equals("EXCEL2007"))
            suffix = "xlsx";

        String outFileStr = eachDirStr + fileSeparator + eachFileName.substring(0, eachFileName.lastIndexOf('.'))
                + "_CACheck" + "." + suffix;

        FileOutputStream outFile = new FileOutputStream(outFileStr);
        workbook.write(outFile);
        outFile.close();
    }

    private static StatisticsForSheet handleRequestForSheet(String eachFileName, Sheet sheet, BufferedWriter writer,
                                                            AnalysisPattern analysisPattern)
            throws IOException, InterruptedException {

        StatisticsForSheet staSheet = new StatisticsForSheet(sheet);
        staSheet.setBeginTime  (System.currentTimeMillis());
        staSheet.setSpreadsheet(eachFileName);
        staSheet.setWorksheet  (sheet.getSheetName());

        try {
            GroundTruthStatistics groundTruthStatistics = new GroundTruthStatistics();
            groundTruthStatistics.read(groundTruthPath + fileSeparator + eachFileName, sheet.getSheetName());
            staSheet.setGt_clusterList(groundTruthStatistics.clusterList);
            staSheet.setGt_smellList(groundTruthStatistics.smellList);

        } catch (Exception e) {
            e.printStackTrace();
        }

        List<CAResult> allCARs = new ArrayList<>();

        AMSheet amSheet = Utils.extractSheet(sheet, eachFileName);
        ExtractSnippet extractSnippet = new ExtractSnippet(amSheet);
        List<Snippet> snippets = extractSnippet.extractSnippet();
        for (Snippet snippet : snippets) {
            List<CAResult> CAResults = ExcelAnalysis.processSnippet(eachFileName, amSheet,
                    snippet, snippets, Log.writer, analysisPattern);
            allCARs.addAll(CAResults);
        }

        //TODO: transform cell array to cluster; transform Cell to CellReference.


        List<Cluster> stageIIClusters = new ArrayList<>();
        List<Smell> smells = new ArrayList<>();

        for (CAResult car: allCARs)
        {
            //transform cell array to cluster
            CellArray ca = car.cellArray;
            Cluster root = new Cluster("RootCluster");
            if (ca.isRowCA)
            {
                for (int i= ca.start; i<=ca.end; i++)
                {
                    Cell cell = sheet.getRow(ca.rowOrColumn).getCell(i);
                    if (cell == null) continue;
                    Cluster cluster = new Cluster(new CellReference(cell).formatAsString());
                    root.addChild(cluster);
                }
            }
            else
            {
                for (int i= ca.start; i<=ca.end; i++)
                {
                    Cell cell = sheet.getRow(i).getCell(ca.rowOrColumn);
                    if (cell == null) continue;
                    Cluster cluster = new Cluster(new CellReference(cell).formatAsString());
                    root.addChild(cluster);
                }
            }

            if ( ! root.isLeaf())
                stageIIClusters.add(root);

            //transform Cell to CellReference.
            for (Cell cell: car.errorCells)
            {
                Smell smell = new Smell(new CellReference(cell));
                if (! smells.contains(smell))
                    smells.add(smell);
            }
            for (Cell cell: car.ambiguousCells)
            {
                Smell smell = new Smell(new CellReference(cell));
                if (! smells.contains(smell))
                    smells.add(smell);
            }
        }

        //System.out.println(sheet.getSheetName()+ " Stop here.");
        //Thread.sleep(2000);
        for (Cluster cluster : stageIIClusters) {
            cluster.extractCellRefs(cluster, 2);
            cluster.extractCells(sheet, cluster, 2);
        }

        BasicUtility bu = new BasicUtility();
        bu.clusterPrintMark(stageIIClusters, sheet);
        bu.smellyCellMark(sheet.getWorkbook(), sheet, smells);

        staSheet.setStageIIClusters(stageIIClusters);
        staSheet.setSmellyCells(smells);

        staSheet.calculateForDetection();
        staSheet.calculateForSmell();
        staSheet.setEndTime(System.currentTimeMillis());

        return staSheet;
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
