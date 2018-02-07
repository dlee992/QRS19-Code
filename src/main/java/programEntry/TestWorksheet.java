package programEntry;

import clustering.bootstrappingClustering.BootstrappingClustering;
import clustering.bootstrappingClustering.FeatureCellMatrix;
import clustering.hacClustering.HacClustering;
import clustering.smellDetectionClustering.SmellDetectionClustering;
import entity.Cluster;
import entity.InfoOfSheet;
import experiment.GroundTruthStatistics;
import experiment.StatisticsForSheet;
import featureExtraction.FeatureExtraction;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;
import utility.BasicUtility;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

import static programEntry.GP.*;
import static programEntry.MainClass.groundTruthPath;
import static programEntry.MainClass.numberOfFormula;

public class TestWorksheet implements Runnable {

    public StatisticsForSheet staSheet;

    private String fileName;
    private Sheet sheet;
    private BufferedWriter logBuffer;
    private boolean test;
    private String category;
    private String categoryDirStr;
    public long beginTime = -1;


    public TestWorksheet(String fileName, Sheet sheet, BufferedWriter logBuffer,
                         boolean test, String category, String categoryDirStr) {
        this.fileName = fileName;
        this.sheet = sheet;
        this.logBuffer = logBuffer;
        this.test = test;
        this.category = category;
        this.categoryDirStr = categoryDirStr;
        staSheet = new StatisticsForSheet(sheet, category, 0);
    }


//    @Override
//    public StatisticsForSheet call() throws Exception {
//        return testWorksheet();
//    }


    @Override
    public void run() {
        try {
            testWorksheet();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            semaphore.release();
        }
    }


    private StatisticsForSheet testWorksheet()
            throws Exception, OutOfMemoryError {

        this.beginTime = System.nanoTime();

        staSheet.setBeginTime  (System.nanoTime());
        staSheet.setSpreadsheet(fileName);
        staSheet.setWorksheet  (sheet.getSheetName());
        staSheet.categoryDirStr = categoryDirStr;
        staSheet.fileName = fileName;

        GroundTruthStatistics groundTruthStatistics = new GroundTruthStatistics();
        if (!test) {
            groundTruthStatistics.read(groundTruthPath + fileSeparator + fileName, sheet.getSheetName());
            staSheet.setGt_clusterList(groundTruthStatistics.clusterList);
            staSheet.setGt_smellList(groundTruthStatistics.smellList);
        }

        System.out.println("[" + Thread.currentThread().getName() + "]: ----Sheet '" + sheet.getSheetName() + "'----");
        //logBuffer.write("----Sheet '" + sheet.getSheetName() + "'----");
        //logBuffer.newLine();
        BasicUtility bu = new BasicUtility();

        InfoOfSheet infoOfSheet = bu.infoExtractedPOI(sheet);

        Map<String, List<String>> formulaInfoList = infoOfSheet.getFormulaMap();
        numberOfFormula.addAndGet(formulaInfoList.size());

        if (formulaInfoList.size() == 0) {
            System.out.println("[" + Thread.currentThread().getName() +
                    "]: Worksheet does not contain formula cells, so skip it.");
            //虽然没有公式，但是也要在完成的worksheet数量上减一
            printLastSheet();
            return staSheet;
        }

        HacClustering hacCluster = new HacClustering(sheet, formulaInfoList);

        List<Cluster> caCheckCluster = new ArrayList<>();
        Set<String> caCheckFormula = new HashSet<>();

        //TODO: 2 from Cell Array to clusters in the HAC algorithm to generate seed clusters
        System.out.println("---- Stage I begun "+ new BasicUtility().getCurrentTime() +"----");
        //logBuffer.write("---- Stage I begun ----");
        //logBuffer.newLine();

        List<Cluster> stageIClusters = hacCluster.clustering();

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

            if (Thread.interrupted()) {
                throw new InterruptedException();
            }

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

            if (Thread.interrupted()) {
                throw new InterruptedException();
            }

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
            bu.clusterMark(stageIIClusters, sheet);
            bu.smellyCellMark(sheet.getWorkbook(), sheet, sdc.getDetectedSmellyCells());

            //Evaluation
            staSheet.setStageIIClusters(stageIIClusters);
            staSheet.setSmellyCells(sdc.getDetectedSmellyCells());
        }
        else {
            System.out.println("Seed cluster does not exist.");
            //logBuffer.write("Seed cluster is null.");
            //logBuffer.newLine();
        }

        staSheet.calculateForDetection();
        staSheet.calculateForSmell();
        staSheet.setEndTime(System.nanoTime());

        System.out.println("---Finished Analysis: " + sheet.getSheetName() + " " + new BasicUtility().getCurrentTime() + "---");
        System.out.println("[" + Thread.currentThread().getName() + "]: ---FinishedWS = " + finishedWS.incrementAndGet());
        System.out.println();
        //logBuffer.write("----Finished Analysis---");
        //logBuffer.newLine();
        //logBuffer.newLine();

        //在这里单独输出每个有意义的被标记的worksheet
        //似乎workbook在多个线程共享的时候，进行并发的写操作，出现了问题，修改没有反应到最后的输出结果中

        printLastSheet();
        return staSheet;
    }

    public synchronized void printLastSheet() throws IOException {

        Workbook workbook = sheet.getWorkbook();
        int currentFlag = 0;
        try {
            currentFlag = printFlag.get(fileName).decrementAndGet();
        }
        catch (NullPointerException ignored) {
            //TODO：我不清楚这里为什么会报告空指针异常，先忽略吧。
            System.out.println("TestWorksheet.java line 254: NullPointerE_xception");
        }
        if (currentFlag > 0) return;

        String suffix = null;
        if (workbook.getSpreadsheetVersion().name().equals("EXCEL97")) {
            suffix = "xls";
        }
        else if (workbook.getSpreadsheetVersion().name().equals("EXCEL2007")) {
            suffix = "xlsx";
        }

        String prefix = fileName.substring(0, fileName.lastIndexOf('.'));
        String outFileStr = categoryDirStr + fileSeparator + prefix
                + "_aha_" + GP.addSuffix() + "." + suffix;

        FileOutputStream outFile = new FileOutputStream(outFileStr);
        //TODO: 不清楚为什么workbook会空指针
        if (workbook == null || outFile == null) return;
        workbook.write(outFile);
        outFile.close();
        workbook.close();
        printFlag.remove(fileName);
    }
}
