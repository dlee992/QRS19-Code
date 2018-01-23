package programEntry;

import ThirdParty.CACheck.amcheck.AnalysisPattern;
import clustering.bootstrappingClustering.BootstrappingClustering;
import clustering.bootstrappingClustering.FeatureCellMatrix;
import clustering.hacClustering.HacClustering;
import clustering.hacClustering.TreeEditDistance;
import clustering.smellDetectionClustering.SmellDetectionClustering;
import com.sun.xml.internal.ws.api.pipe.FiberContextSwitchInterceptor;
import entity.Cluster;
import entity.InfoOfSheet;
import experiment.GroundTruthStatistics;
import experiment.StatisticsForAll;
import experiment.StatisticsForSheet;
import featureExtraction.FeatureExtraction;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.poi.EmptyFileException;
import org.apache.poi.hssf.OldExcelFormatException;
import org.apache.poi.hssf.record.RecordInputStream;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
import utility.BasicUtility;

import javax.swing.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static programEntry.GP.*;
import static programEntry.GP.index;
import static programEntry.TestDataSet.upperLimit;

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

    private static AtomicInteger
            finishedSS = new AtomicInteger(0),
            finishedWS = new AtomicInteger(0);


    private static AtomicInteger numberOfFormula = new AtomicInteger(0);

    private static ArrayList<String> ssNameList = new ArrayList<>();

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

        staAll = new StatisticsForAll();

        for (int i = 0; categories != null && i < categories.length; i++) {
            File perCategory = new File(categories[i].getAbsolutePath());
            File[] files = perCategory.listFiles(filter1);

            assert files != null;

            int cnt = 0;
            for (File eachFile : files) {
                //if (!eachFile.getName().startsWith("0000")) continue;
//                if (!eachFile.getName().startsWith("VRS")) continue;
                if (cnt > 0) break;
                cnt++;

                final File finalEachFile = new File(eachFile.getAbsolutePath());

                try {
                    testSpreadsheet(finalEachFile, staAll, logBuffer, index, false, perCategory.getName());
                }
                catch (Exception | OutOfMemoryError e) {
                    e.printStackTrace();
                }
            }
        }

        //executorDone(exeService, staAll, prefixOutDir, logBuffer, null);
        TimeUnit.MINUTES.sleep(1);
        exeService.shutdown();
        exeService.awaitTermination(1, TimeUnit.DAYS);
        staAll.log(prefixOutDir, false, null);
    }



    public static void executorDone(ExecutorService executorService, StatisticsForAll staAll,
                                    String staResult, BufferedWriter logBuffer, List<String> errorExcelList)
            throws InterruptedException, IOException {

        System.out.println("Post-processing begins.");
        staAll.log(staResult, false, errorExcelList);

        try {
            //logBuffer.flush();
            //logBuffer.close();
            //System.out.println("logBuffer is closed.\n");

            int i = 0;
            for (String fileName:
                 ssNameList) {
                System.out.println(++i +": "+ fileName);
            }
        }
        catch (NullPointerException npe) {
            npe.printStackTrace();
        }

        //createAndShowGUI();
        System.out.println("Post-processing finishes.");
    }

    public static void createAndShowGUI() {
        JFrame frame = new JFrame("程序结束");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JLabel label = new JLabel("程序结束");
        frame.getContentPane().add(label);

        frame.pack();
        frame.setVisible(true);
    }

    public static void testSpreadsheet(File file, StatisticsForAll staAll, BufferedWriter logBuffer,
                                       AtomicInteger index, boolean test, String category)
            throws Exception, OutOfMemoryError {

        int identicalIndex = index.incrementAndGet();
        System.out.println("index = " + identicalIndex + " ######## begin: " +
                 "/" + file.getName() + "'########");
        //logBuffer.write("index = " + identicalIndex + " ######## begin: " +
                 //"/" + file.getName() + "'########");
        //logBuffer.newLine();

        if (identicalIndex > upperLimit) return;

        String fileName = null;
        Workbook workbook;
        try {
            fileName = file.getName();
            workbook = WorkbookFactory.create(new FileInputStream(file));
        }
        catch (OldExcelFormatException oefe) {
            System.out.println("Old Excel Format E-xception happened.");
            workbook = null;
        }
        catch (InvalidFormatException ife) {
            System.out.println("Invalid Format E-xception happened.");
            workbook = null;
        }
        catch (EmptyFileException EFE) {
            System.out.println("MainClass @200 line: Empty File E-xception.");
            workbook = null;
        }
        catch (RecordInputStream.LeftoverDataException LDE) {
            System.out.println("MainClass @200 line: Left Over Data E-xception.");
            workbook = null;
        }

        if (workbook == null) {
            System.out.println("Spreadsheet index = "+ identicalIndex +" ######## End in: '" + fileName + "'########");
            System.out.println("FinishedSS = " + finishedSS.incrementAndGet());
            System.out.println();
//            logBuffer.write("Spreadsheet index = "+ identicalIndex +" ######## End in: " + fileName + "'########");
            ssNameList.add(fileName);
            addVirtualSS(category, fileName, 1);
            return;
        }

        AtomicBoolean flagAdd = new AtomicBoolean(false);
        for (int j = 0; j < workbook.getNumberOfSheets(); j++) {
            //TODO: test the specific worksheet
//            if (!workbook.getSheetAt(j).getSheetName().contains("Table II.4")) continue;

            String finalFileName = fileName;
            Sheet curSheet = workbook.getSheetAt(j);

            exeService.execute(() -> {
                try {
                    StatisticsForSheet staSheet = testWorksheet(finalFileName, curSheet, logBuffer, test, category);
                    if (staAll.add(staSheet, logBuffer))
                        flagAdd.set(true);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }

            });

        }

        if (!flagAdd.get()) {
            addVirtualSS(category, fileName, 2);
        }

        String eachDirStr = outDirPath + fileSeparator + "Marked subjects " + testDate;

        File eachDir = new File(eachDirStr);
        if (!eachDir.exists()) {
            eachDir.mkdir();
        }

        String categoryDirStr = eachDirStr + fileSeparator + category;
        File categoryDir = new File(categoryDirStr);
        if (!categoryDir.exists()) {
            categoryDir.mkdir();
        }

        String suffix = null;
        if (workbook .getSpreadsheetVersion().name().equals("EXCEL97"))
            suffix = "xls";
        else if (workbook .getSpreadsheetVersion().name().equals("EXCEL2007"))
            suffix = "xlsx";
        String outFileStr = categoryDirStr + fileSeparator + fileName.substring(0, fileName.lastIndexOf('.'))
                + "_" + GP.addSuffix() + "." + suffix;

        FileOutputStream outFile = new FileOutputStream(outFileStr);
        workbook.write(outFile);
        outFile.close();

        System.out.println("Spreadsheet index = "+ identicalIndex +" ######## End in: '" + fileName + "'########");
        System.out.println("FinishedSS = " + finishedSS.incrementAndGet());
        System.out.println();
        //logBuffer.write("Spreadsheet index = "+ identicalIndex +" ######## End in: '" + fileName + "'########");
        //logBuffer.newLine();
        ssNameList.add(fileName);

        //在每个SS执行完之后立刻输出当前所有执行完的SS的综合信息
        staAll.log(prefixOutDir, true, null);
    }

    private static void addVirtualSS(String category, String fileName, int situation) throws IOException {
        StatisticsForSheet staSheet = new StatisticsForSheet(null, category, situation);
        staSheet.setSpreadsheet(fileName);
        staAll.add(staSheet, logBuffer);
        staAll.log(prefixOutDir, true, null);
    }


    private static StatisticsForSheet testWorksheet(String fileName, Sheet sheet, BufferedWriter logBuffer,
                                                    boolean test, String category)
            throws Exception, OutOfMemoryError {
        StatisticsForSheet staSheet = new StatisticsForSheet(sheet, category, 0);
        staSheet.setBeginTime  (System.currentTimeMillis());
        staSheet.setSpreadsheet(fileName);
        staSheet.setWorksheet  (sheet.getSheetName());

        GroundTruthStatistics groundTruthStatistics = new GroundTruthStatistics();
        if (!test) {
            groundTruthStatistics.read(groundTruthPath + fileSeparator + fileName, sheet.getSheetName());
            staSheet.setGt_clusterList(groundTruthStatistics.clusterList);
            staSheet.setGt_smellList(groundTruthStatistics.smellList);
        }

        System.out.println("----Sheet '" + sheet.getSheetName() + "'----");
        //logBuffer.write("----Sheet '" + sheet.getSheetName() + "'----");
        //logBuffer.newLine();
        BasicUtility bu = new BasicUtility();

        InfoOfSheet infoOfSheet = bu.infoExtractedPOI(sheet);
        Map<String, List<String>> formulaInfoList = infoOfSheet.getFormulaMap();
        numberOfFormula.addAndGet(formulaInfoList.size());

        HacClustering hacCluster = new HacClustering(formulaInfoList);
        TreeEditDistance ted       = new TreeEditDistance(sheet);
        List<Cluster> caCheckCluster = new ArrayList<>();
        Set<String> caCheckFormula = new HashSet<>();

        //TODO: 2 from Cell Array to clusters in the HAC algorithm to generate seed clusters
        System.out.println("---- Stage I begun "+ new BasicUtility().getCurrentTime() +"----");
        //logBuffer.write("---- Stage I begun ----");
        //logBuffer.newLine();

        List<Cluster> stageIClusters = hacCluster.clustering(ted);

        System.out.println("---- Stage I finished "+ new BasicUtility().getCurrentTime() + "----");
        //logBuffer.write("---- Stage I finished ----");
        //logBuffer.newLine();

        for (Cluster cluster : stageIClusters) {
            cluster.extractCellRefs(cluster, 1);
            cluster.extractCells(sheet,cluster, 1);
            cluster.computeBorders();

            //System.out.print("Stage One Clusters = [");
            for (CellReference leaf: cluster.getClusterCellRefs()) {

                //System.out.printf("%s, ", leaf.formatAsString());
            }
            //System.out.println("]");
        }

        //System.out.printf("Cluster size = %d\n", stageIClusters.size());


        //TODO: 3 weak feature extraction
        FeatureExtraction fe = new FeatureExtraction(sheet, stageIClusters);
        fe.featureExtractionFromSheet(infoOfSheet.getDataCells());

        List<Cluster> seedClusters = fe.getSeedCluster();
        if (seedClusters != null && !seedClusters.isEmpty()) {
            FeatureCellMatrix fcc = new FeatureCellMatrix(
                    fe.getFeatureVectorForClustering(),
                    fe.getCellRefsVector());

            RealMatrix featureCellM = fcc.matrixCreationForClustering(fe.getCellFeatureList());

            System.out.println("---- Stage II begin "+ new BasicUtility().getCurrentTime() + "----");
            //logBuffer.write("---- Stage II begin ----");
            //logBuffer.newLine();

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

                //System.out.printf("Stage Two Clusters = [");
                for (CellReference leaf: cluster.getClusterCellRefs()) {
                    //System.out.printf("%s, ", leaf.formatAsString());
                }
                //System.out.println("]");
            }

            System.out.println("---- Stage II finished "+ new BasicUtility().getCurrentTime() + "----");
            //logBuffer.write("---- Stage II finished ----");
            //logBuffer.newLine();

            //TODO: add an additional step, try to extend the cluster composed of 2~3 formula cells using the
            //TODO: "cell array" information, see whether the recall ratio can be improved.
            //Result: not nice precision% = 81, recall% = 82, Tag v1.3.1

            //TODO: 5 local outlier detection
            System.out.println("---- Smell detection begun "+ new BasicUtility().getCurrentTime() + "----");
            //logBuffer.write("---- Smell detection begun ----");
            //logBuffer.newLine();

            SmellDetectionClustering sdc = new SmellDetectionClustering(sheet, stageIIClusters, fe.getCellFeatureList());
            sdc.outlierDetection();

            for (Cluster cluster : stageIClusters) {

                //System.out.printf("(%.3f) Smell Detection Clusters = [", cluster.coverage);
                //logBuffer.write("(" + cluster.coverage + ") Smell Detection Clusters = [");
                for (CellReference leaf: cluster.getClusterCellRefs()) {

                    //System.out.printf("%s, ", leaf.formatAsString());
                    //logBuffer.write(leaf.formatAsString() + ", ");
                }
                //System.out.println("]");
                //logBuffer.write("]");
                //logBuffer.newLine();
            }

            //logBuffer.write("---- Smell detection finished ----");
            //logBuffer.newLine();

            //TODO: 6 mark the worksheet
            bu.clusterPrintMark(stageIIClusters, sheet);
            bu.smellyCellMark(sheet.getWorkbook(), sheet, sdc.getDetectedSmellyCells());

            //Evaluation
            staSheet.setStageIIClusters(stageIIClusters);
            staSheet.setSmellyCells(sdc.getDetectedSmellyCells());
        }
        else {
            System.out.println("Seed cluster is null.");
            //logBuffer.write("Seed cluster is null.");
            //logBuffer.newLine();
        }

        staSheet.calculateForDetection();
        staSheet.calculateForSmell();
        staSheet.setEndTime(System.currentTimeMillis());

        System.out.println("---Finished Analysis"+ new BasicUtility().getCurrentTime() + "---");
        System.out.println("---FinishedWS = " + finishedWS.incrementAndGet());
        System.out.println();
        //logBuffer.write("----Finished Analysis---");
        //logBuffer.newLine();
        //logBuffer.newLine();
        return staSheet;
    }
}
