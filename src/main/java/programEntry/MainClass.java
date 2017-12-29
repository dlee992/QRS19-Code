package programEntry;

import ThirdParty.CACheck.AMSheet;
import ThirdParty.CACheck.CellArray;
import ThirdParty.CACheck.amcheck.AnalysisPattern;
import ThirdParty.CACheck.amcheck.ExcelAnalysis;
import ThirdParty.CACheck.cellarray.extract.CAResult;
import ThirdParty.CACheck.snippet.ExtractSnippet;
import ThirdParty.CACheck.snippet.Snippet;
import ThirdParty.CACheck.util.Log;
import ThirdParty.CACheck.util.Utils;
import clustering.bootstrappingClustering.BootstrappingClustering;
import clustering.bootstrappingClustering.FeatureCellMatrix;
import clustering.hacClustering.HacClustering;
import clustering.hacClustering.TreeEditDistance;
import clustering.smellDetectionClustering.SmellDetectionClustering;
import entity.Cluster;
import entity.InfoOfSheet;
import experiment.GroundTruthStatistics;
import experiment.StatisticsForAll;
import experiment.StatisticsForSheet;
import featureExtraction.FeatureExtraction;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.poi.hssf.OldExcelFormatException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
import utility.BasicUtility;

import javax.swing.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static programEntry.GP.*;
import static programEntry.GP.index;

/**
 * Created by lida on 2017/6/27.
 */
public class MainClass {
    //TODO: get the file directory
    //TODO: get the specific spreadsheet file
    //TODO: get the specific worksheet
    //TODO: from ThirdParty.CACheck get "Cell Array", actually which is contained in "CAResult"
    //TODO: from Cell Array to clusters in the HAC algorithm to generate seed clusters
    //TODO: the remainder is the same as CC


    private static String groundTruthPath;
    private static String mode;
    private static String inDirPath;
    private static String programState;

    private static AtomicInteger numberOfFormula = new AtomicInteger(0);

    private static AnalysisPattern analysisPattern = new AnalysisPattern();

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

        //TODO: for ThirdParty.CACheck
        analysisPattern.setType(3);

        for (int i = 0; categories != null && i < categories.length; i++) {
            File perCategory = new File(categories[i].getAbsolutePath());
            File[] files = perCategory.listFiles(filter1);

            assert files != null;
            for (File eachFile : files) {
                if (eachFile.getName().startsWith("0000")) continue;
//                if (!eachFile.getName().startsWith("VRS")) continue;

                final File finalEachFile = new File(eachFile.getAbsolutePath());

                exeService.execute(() -> {
                    try {
                        testSpreadsheet(finalEachFile, staAll, logBuffer, index);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        }

        executorDone(exeService, staAll, staResult, logBuffer);
    }



    public static void executorDone(ExecutorService executorService, StatisticsForAll staAll,
                                    String staResult, BufferedWriter logBuffer)
            throws InterruptedException, IOException {

        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.DAYS);

        staAll.setEndTime(System.currentTimeMillis());
        staAll.log(staResult);

//        System.out.println(staResult);
//        System.out.println("formula numbers : " + numberOfFormula);
        logBuffer.flush();
        logBuffer.close();
        System.out.println("logBuffer is closed.");

        createAndShowGUI();
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("程序结束");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JLabel label = new JLabel("程序结束");
        frame.getContentPane().add(label);

        frame.pack();
        frame.setVisible(true);
    }

    public static void testSpreadsheet(File file, StatisticsForAll staAll, BufferedWriter logBuffer, AtomicInteger index)
            throws Exception {
//        if (index.get() >= 2) return;

        System.out.println("index = " +(index.incrementAndGet())+ " ########Process '" +
                 "/" + file.getName() + "'########");
        logBuffer.write("index = " +(index.get())+ " ########Process '" +
                 "/" + file.getName() + "'########");
        logBuffer.newLine();


        String fileName = null;
        Workbook workbook = null;
        try {
            fileName = file.getName();
            workbook = WorkbookFactory.create(new FileInputStream(file));
        }
        catch (OldExcelFormatException oefe) {
            System.out.println("Old Excel Format Exception happened.");
        }
        catch (InvalidFormatException ife) {
            System.out.println("Invalid Format Exception happened.");
        }

        if (workbook == null) return;

        for (int j = 0; j < workbook.getNumberOfSheets(); j++) {
            //TODO: test the specific worksheet
//            if (!workbook.getSheetAt(j).getSheetName().contains("Table II.4")) continue;

            StatisticsForSheet staSheet = testWorksheet(fileName, workbook.getSheetAt(j), logBuffer);
            staAll.add(staSheet, logBuffer);
        }

        String eachDirStr = outDirPath + fileSeparator + "Marked subjects " + testDate;

        File eachDir = new File(eachDirStr);
        if (!eachDir.exists()) {
            eachDir.mkdirs();
        }

        String suffix = null;
        if (workbook .getSpreadsheetVersion().name().equals("EXCEL97"))
            suffix = "xls";
        else if (workbook .getSpreadsheetVersion().name().equals("EXCEL2007"))
            suffix = "xlsx";
        String outFileStr = eachDirStr + fileSeparator + fileName.substring(0, fileName.lastIndexOf('.'))
                + "_marked" + GP.addSuffix() + "." + suffix;

        FileOutputStream outFile = new FileOutputStream(outFileStr);
        workbook.write(outFile);
        outFile.close();

        System.out.println("index = "+ index +" ######## End in: '" + file.getName() + "'########");
        logBuffer.write("index = "+ index +" ######## End in: '" + file.getName() + "'########");
        logBuffer.newLine();
    }

    private static StatisticsForSheet testWorksheet(String fileName, Sheet sheet, BufferedWriter logBuffer)
            throws Exception {
        StatisticsForSheet staSheet = new StatisticsForSheet(sheet);
        staSheet.setBeginTime  (System.currentTimeMillis());
        staSheet.setSpreadsheet(fileName);
        staSheet.setWorksheet  (sheet.getSheetName());

        GroundTruthStatistics groundTruthStatistics = new GroundTruthStatistics();
        groundTruthStatistics.read(groundTruthPath + fileSeparator + fileName, sheet.getSheetName());
        staSheet.setGt_clusterList(groundTruthStatistics.clusterList);
        staSheet.setGt_smellList(groundTruthStatistics.smellList);

        System.out.println("----Sheet '" + sheet.getSheetName() + "'----");
        logBuffer.write("----Sheet '" + sheet.getSheetName() + "'----");
        logBuffer.newLine();
        BasicUtility bu = new BasicUtility();

        InfoOfSheet infoOfSheet = bu.infoExtractedPOI(sheet);
        Map<String, List<String>> formulaInfoList = infoOfSheet.getFormulaMap();
        numberOfFormula.addAndGet(formulaInfoList.size());

        HacClustering hacCluster = new HacClustering(formulaInfoList);
        TreeEditDistance ted       = new TreeEditDistance(sheet);
        List<Cluster> caCheckCluster = new ArrayList<>();
        Set<String> caCheckFormula = new HashSet<>();
        List<CAResult> allCARs = new ArrayList<>();

        if (GP.plusCellArray) {
            //TODO: 1 from ThirdParty.CACheck get "Cell Array", actually which is contained in "CAResult"
            AMSheet amSheet = Utils.extractSheet(sheet, fileName);
            ExtractSnippet extractSnippet = new ExtractSnippet(amSheet);
            List<Snippet> snippets = extractSnippet.extractSnippet();
            for (Snippet snippet : snippets) {
                List<CAResult> CAResults = ExcelAnalysis.processSnippet(fileName, amSheet,
                    snippet, snippets, Log.writer, analysisPattern);
                allCARs.addAll(CAResults);
            }

            int clusterCount = 0;
            for (CAResult car : allCARs) {
                //TODO: transform cell array to cluster
                CellArray ca = car.cellArray;
                Cluster root = new Cluster("caCheck " + ++clusterCount);
                if (ca.isRowCA) {
                    for (int i = ca.start; i <= ca.end; i++) {
                        Cell cell = sheet.getRow(ca.rowOrColumn).getCell(i);
                        if (cell == null) continue;

                        //TODO: the cell is a formula cell, instead of a data cell
                        if (cell.getCellType() == 2) //CellType = FORMULA
                        {
                            Cluster cluster = new Cluster(new CellReference(cell).formatAsString());
                            root.addChild(cluster);
                            String cellAddress = new CellReference(cell).formatAsString();
                            caCheckFormula.add(cellAddress);
                            //System.out.printf("%s ", cellAddress);
                        }
                    }
                } else {
                    for (int i = ca.start; i <= ca.end; i++) {
                        Cell cell = sheet.getRow(i).getCell(ca.rowOrColumn);
                        if (cell == null) continue;

                        //TODO: the cell is a formula cell, instead of a data cell
                        if (cell.getCellType() == 2) //CellType = FORMULA
                        {
                            Cluster cluster = new Cluster(new CellReference(cell).formatAsString());
                            root.addChild(cluster);
                            String cellAddress = new CellReference(cell).formatAsString();
                            caCheckFormula.add(cellAddress);
                            // System.out.printf("%s ", cellAddress);
                        }
                    }
                }

                clusterCount--;
                if (!root.isLeaf()) {
                    caCheckCluster.add(root);
                    //System.out.println();
                    clusterCount++;

                    System.out.printf("Cell Array = [");
                    for (Cluster cluster : root.getChildren()) {
                        System.out.printf("%s, ", cluster.getName());
                    }
                    System.out.println("]");

                }
            }
        }

        //TODO: 2 from Cell Array to clusters in the HAC algorithm to generate seed clusters
        System.out.println("---- Stage I begun ----");
        logBuffer.write("---- Stage I begun ----");
        logBuffer.newLine();

        hacCluster.computeDistance(ted);
        List<Cluster> stageIClusters;
        if (GP.plusCellArray)
            stageIClusters = hacCluster.clusteringWrapper(caCheckCluster, caCheckFormula);
        else
            stageIClusters = hacCluster.clustering(ted);

        System.out.println("---- Stage I finished ----");
        logBuffer.write("---- Stage I finished ----");
        logBuffer.newLine();

        for (Cluster cluster : stageIClusters) {
            cluster.extractCellRefs(cluster, 1);
            cluster.extractCells(sheet,cluster, 1);
            cluster.computeBorders();

            System.out.printf("Stage One Clusters = [");
            for (CellReference leaf: cluster.getClusterCellRefs()) {

                System.out.printf("%s, ", leaf.formatAsString());
            }
            System.out.println("]");
        }

        System.out.printf("Cluster size = %d\n", stageIClusters.size());


        //TODO: 3 weak feature extraction
        FeatureExtraction fe = new FeatureExtraction(sheet, stageIClusters);
        fe.featureExtractionFromSheet(infoOfSheet.getDataCells());

        List<Cluster> seedClusters = fe.getSeedCluster();
        if (seedClusters != null && !seedClusters.isEmpty()) {
            FeatureCellMatrix fcc = new FeatureCellMatrix(
                    fe.getFeatureVectorForClustering(),
                    fe.getCellRefsVector());

            RealMatrix featureCellM = fcc.matrixCreationForClustering(fe.getCellFeatureList());

            System.out.println("---- Stage II begin ----");
            logBuffer.write("---- Stage II begin ----");
            logBuffer.newLine();

            // If there is isolated cells
            List<CellReference> nonSeedCellRefs = fe.getNonSeedCellRefs();
            List<Cell>          nonSeedCells    = fe.getNonSeedCells();

            //TODO: 4 second-stage clustering
            BootstrappingClustering bc = new BootstrappingClustering(fe, sheet);
            RealMatrix cellClusterM    = bc.clustering(featureCellM);
            List<Cluster> stageIIClusters;

            if (nonSeedCells != null && !nonSeedCells.isEmpty()) {
                double threshold = 0.5;
                stageIIClusters = bc.addCellToCluster(
                        cellClusterM,
                        nonSeedCellRefs,
                        nonSeedCells, threshold);
            }
            else {
                stageIIClusters = stageIClusters;
            }

            for (Cluster cluster : stageIIClusters) {
                cluster.extractCellRefs(cluster, 2);
                cluster.extractCells(sheet, cluster, 2);

                System.out.printf("Stage Two Clusters = [");
                for (CellReference leaf: cluster.getClusterCellRefs()) {
                    System.out.printf("%s, ", leaf.formatAsString());
                }
                System.out.println("]");
            }

            System.out.println("---- Stage II finished ----");
            logBuffer.write("---- Stage II finished ----");
            logBuffer.newLine();

            //TODO: add an additional step, try to extend the cluster composed of 2~3 formula cells using the
            //TODO: "cell array" information, see whether the recall ratio can be improved.
            //Result: not nice precision% = 81, recall% = 82, Tag v1.3.1

            if (GP.plusExtendSmallClu) {
                for (Cluster cluster : stageIIClusters) {
                    if (cluster.getClusterCellRefs().size() > 3) continue;

                    //FIXME: whether need this following constraint?
                    boolean allFormula = true;
                    for (Cell cell : cluster.getClusterCells()) {
                        if (cell.getCellType() == 0) // data cell
                            allFormula = false;
                    }
                    if (!allFormula) continue;

                    //
                    for (CAResult car : allCARs) {
                        CellArray ca = car.cellArray;

                        //TODO: whether this ca contains at least one formula cell in this cluster
                        boolean containCluster = false;
                        for (CellReference cellR : cluster.getClusterCellRefs()) {
                            if (ca.isRowCA) {
                                if (ca.rowOrColumn == cellR.getRow() &&
                                        ca.start <= cellR.getCol() && cellR.getCol() <= ca.end)
                                    containCluster = true;
                            } else {
                                if (ca.rowOrColumn == cellR.getCol() &&
                                        ca.start <= cellR.getRow() && cellR.getRow() <= ca.end)
                                    containCluster = true;
                            }
                        }
                        if (!containCluster) continue;

                        //
                        if (ca.isRowCA) {
                            for (int i = ca.start; i <= ca.end; i++) {
                                Cell cell = sheet.getRow(ca.rowOrColumn).getCell(i);
                                if (cell == null) continue;

                                //TODO: need data cells
                                if (cell.getCellType() == 0) {
                                    cluster.extractCells(sheet, cluster, 2);
                                    if (!cluster.getClusterCells().contains(cell))
                                        cluster.addChild(new Cluster(new CellReference(cell).formatAsString()));
                                }
                            }
                        } else {
                            for (int i = ca.start; i <= ca.end; i++) {
                                Cell cell = sheet.getRow(i).getCell(ca.rowOrColumn);
                                if (cell == null) continue;

                                //TODO: the cell is a formula cell, instead of a data cell
                                if (cell.getCellType() == 0) {
                                    cluster.extractCells(sheet, cluster, 2);
                                    if (!cluster.getClusterCells().contains(cell))
                                        cluster.addChild(new Cluster(new CellReference(cell).formatAsString()));
                                }
                            }
                        }

                        cluster.extractCellRefs(cluster, 2);
                        cluster.extractCells(sheet, cluster, 2);
                    }
                }
            }

            //TODO: 5 local outlier detection
            System.out.println("---- Smell detection begun ----");
            logBuffer.write("---- Smell detection begun ----");
            logBuffer.newLine();

            SmellDetectionClustering sdc = new SmellDetectionClustering(sheet, stageIIClusters, fe.getCellFeatureList());
            sdc.outlierDetection();

            for (Cluster cluster : stageIClusters) {

                System.out.printf("(%.3f) Smell Detection Clusters = [", cluster.coverage);
                logBuffer.write("(" + cluster.coverage + ") Smell Detection Clusters = [");
                for (CellReference leaf: cluster.getClusterCellRefs()) {

                    System.out.printf("%s, ", leaf.formatAsString());
                    logBuffer.write(leaf.formatAsString() + ", ");
                }
                System.out.println("]");
                logBuffer.write("]");
                logBuffer.newLine();
            }

            System.out.println("---- Smell detection finished ----");
            logBuffer.write("---- Smell detection finished ----");
            logBuffer.newLine();

            //TODO: 6 mark the worksheet
            bu.clusterPrintMark(stageIIClusters, sheet);
            bu.smellyCellMark(sheet.getWorkbook(), sheet, sdc.getDetectedSmellyCells());

            //Evaluation
            staSheet.setStageIIClusters(stageIIClusters);
            staSheet.setSmellyCells(sdc.getDetectedSmellyCells());
        }
        else {
            System.out.println("Seed cluster is null.");
            logBuffer.write("Seed cluster is null.");
            logBuffer.newLine();
        }

        staSheet.calculateForDetection();
        staSheet.calculateForSmell();
        staSheet.setEndTime(System.currentTimeMillis());

        System.out.println("---Finished Analysis---");
        logBuffer.write("----Finished Analysis---");
        logBuffer.newLine();
        logBuffer.newLine();
        return staSheet;
    }
}