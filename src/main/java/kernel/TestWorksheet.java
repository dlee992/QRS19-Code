package kernel;

import clustering.bootstrappingClustering.BootstrappingClustering;
import clustering.bootstrappingClustering.FeatureCellMatrix;
import clustering.hacClustering.HacClustering;
import clustering.smellDetectionClustering.SmellDetectionClustering;
import entity.Cluster;
import entity.InfoOfSheet;
import extraction.FeatureExtraction;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;
import statistics.GroundTruthStatistics;
import statistics.StatisticsForSheet;
import utility.BasicUtility;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.List;
import java.util.Map;

import static kernel.GP.*;

public class TestWorksheet implements Runnable {

    public StatisticsForSheet staSheet;
    public long threadID;
    public boolean alreadyDone = false;
    public long threadCPUTime = -1;

    private String fileName;
    private Sheet sheet;
    private BufferedWriter logBuffer;
    private boolean test;
    private String category;
    private String categoryDirStr;
    public long beginTime = -1;
    public long endTime = -1;

    private Object lockForSS;

    private volatile Thread blinker;


    public TestWorksheet(String fileName, Sheet sheet, BufferedWriter logBuffer,
                         boolean test, String category, String categoryDirStr, Object lockForSS) {
        this.fileName = fileName;
        this.sheet = sheet;
        this.logBuffer = logBuffer;
        this.test = test;
        this.category = category;
        this.categoryDirStr = categoryDirStr;
        staSheet = new StatisticsForSheet(sheet, category, 0);
        this.lockForSS = lockForSS;
    }

    public void stop() {
        blinker = null;
    }

    @Override
    public void run() {
        ThreadMXBean monitor = ManagementFactory.getThreadMXBean();
        try {
            blinker = Thread.currentThread();
            //System.out.println(Thread.currentThread().getName() + ": Spreadsheet = " + fileName + ", sheet name = " + sheet.getSheetName() + ": Begin");
            threadID = Thread.currentThread().getId();
            this.beginTime = monitor.getThreadCpuTime(Thread.currentThread().getId())  / 1000_000_000;
            testWorksheet();
            alreadyDone = true;
            this.endTime = monitor.getThreadCpuTime(Thread.currentThread().getId())  / 1000_000_000;
            this.threadCPUTime = this.endTime - this.beginTime;

        } catch (Exception e) {
            e.printStackTrace();
            alreadyDone = true;
            this.endTime = monitor.getThreadCpuTime(Thread.currentThread().getId())  / 1000_000_000;
            this.threadCPUTime = this.endTime - this.beginTime;
        } finally {
            semaphore.release();
            alreadyDone = true;
        }
    }


    private void testWorksheet()
            throws Exception, OutOfMemoryError {

        /*
         * TODO: 专门开启一个线程去限定这个线程的运行时间，当然还是通过软中断的方式。
         */
//        TimeoutForSheet timeoutForSheet = new TimeoutForSheet(Thread.currentThread());
//        new Thread(timeoutForSheet).start();

        try {


            staSheet.setBeginTime(System.nanoTime());
            staSheet.setSpreadsheet(fileName);
            staSheet.setWorksheet(sheet.getSheetName());
            staSheet.categoryDirStr = categoryDirStr;
            staSheet.fileName = fileName;

            GroundTruthStatistics groundTruthStatistics = new GroundTruthStatistics();
            if (!test) {
                String groundTruthPath = GP.parent_dir + fileSeparator + "Inputs" + fileSeparator + "Groundtruth";
                groundTruthStatistics.read(groundTruthPath + fileSeparator + fileName, sheet.getSheetName());
                staSheet.setGt_clusterList(groundTruthStatistics.clusterList);
                staSheet.setGt_smellList(groundTruthStatistics.smellList);


            }

            System.out.println("[" + Thread.currentThread().getName() + "]: Sheet " + sheet.getSheetName()
                    + " starts at " + new BasicUtility().getCurrentTime());
            //logBuffer.write("----Sheet '" + sheet.getSheetName() + "'----");
            //logBuffer.newLine();
            BasicUtility bu = new BasicUtility();

            InfoOfSheet infoOfSheet = bu.infoExtractedPOI(blinker, sheet, beginTime);

            Map<String, List<String>> formulaInfoList = infoOfSheet.getFormulaMap();
//            numberOfFormula.addAndGet(formulaInfoList.size());

            if (formulaInfoList.size() == 0) {
                finishedWS.incrementAndGet();
                //System.out.println(Thread.currentThread().getName() +
                //        ": Spreadsheet = " + staSheet.fileName + ", sheet name = " + sheet.getSheetName() +
                //        ", worksheet index = " + finishedWS.incrementAndGet());
                //虽然没有公式，但是也要在完成的worksheet数量上减一
                //printLastSheet();
                return;
            }

            HacClustering hacCluster = new HacClustering(blinker, sheet, formulaInfoList, beginTime);

            //TODO: 2 from Cell Array to clusters in the HAC algorithm to generate seed clusters
            System.out.println("[" + Thread.currentThread().getName() + "]: Sheet " + sheet.getSheetName()
                    + " Stage I starts at " + new BasicUtility().getCurrentTime() );
            //logBuffer.write("---- Stage I begun ----");
            //logBuffer.newLine();

            List<Cluster> stageIClusters = hacCluster.clustering();

            System.out.println("[" + Thread.currentThread().getName() + "]: Sheet " + sheet.getSheetName()
                    + " Stage I finishes at " + new BasicUtility().getCurrentTime());

            for (Cluster cluster : stageIClusters) {
                cluster.extractCellRefs(cluster, 1);
                cluster.extractCells(sheet, cluster, 1);
                cluster.computeBorders();

            }

            //3 weak feature extraction
            System.out.println("[" + Thread.currentThread().getName() + "]: Sheet " + sheet.getSheetName()
                    + " Stage II (fe) starts at " + new BasicUtility().getCurrentTime());
            FeatureExtraction fe = new FeatureExtraction(blinker, sheet, stageIClusters, beginTime);
            fe.featureExtractionFromSheet(infoOfSheet.getDataCells());

            System.out.println("[" + Thread.currentThread().getName() + "]: Sheet " + sheet.getSheetName()
                    + " Stage II (fe) finishes at " + new BasicUtility().getCurrentTime());


            List<Cluster> seedClusters = fe.getSeedCluster();
            if (seedClusters != null && !seedClusters.isEmpty()) {
                FeatureCellMatrix fcc = new FeatureCellMatrix(blinker,
                        fe.getFeatureVectorForClustering(),
                        fe.getCellRefsVector(), beginTime);

                RealMatrix featureCellM = fcc.matrixCreationForClustering(fe.getCellFeatureList());

                System.out.println("[" + Thread.currentThread().getName() + "]: Sheet " + sheet.getSheetName()
                        + " Stage II (boost) starts at " + new BasicUtility().getCurrentTime());

                List<CellReference> nonSeedCellRefs = fe.getNonSeedCellRefs();
                List<Cell> nonSeedCells = fe.getNonSeedCells();

                //TODO: 4 second-stage clustering
                BootstrappingClustering bc = new BootstrappingClustering(blinker, fe, sheet, beginTime);
                RealMatrix cellClusterM = bc.clustering(featureCellM);
                List<Cluster> stageIIClusters;

                if (blinker != Thread.currentThread()) {
                    throw new RuntimeException();
                }

                if (nonSeedCells != null && !nonSeedCells.isEmpty()) {
                    double threshold = 0.5;
                    stageIIClusters = bc.addCellToCluster(
                            cellClusterM,
                            nonSeedCellRefs,
                            nonSeedCells, threshold);
                } else {
                    stageIIClusters = stageIClusters;
                }

                for (Cluster cluster : stageIIClusters) {
                    cluster.extractCellRefs(cluster, 2);
                    cluster.extractCells(sheet, cluster, 2);
                }

                System.out.println("[" + Thread.currentThread().getName() + "]: Sheet " + sheet.getSheetName()
                        + " Stage II (boost) finishes at " + new BasicUtility().getCurrentTime());

                //local outlier detection
                System.out.println("[" + Thread.currentThread().getName() + "]: Sheet " + sheet.getSheetName()
                        + " Stage III defect detection starts at " + new BasicUtility().getCurrentTime());

                SmellDetectionClustering sdc = new SmellDetectionClustering(blinker, sheet, stageIIClusters, fe.getCellFeatureList(), beginTime);
                sdc.outlierDetection();

                if (blinker != Thread.currentThread()) {
                    throw new RuntimeException();
                }

                System.out.println("[" + Thread.currentThread().getName() + "]: Sheet " + sheet.getSheetName()
                        + " Stage III defect detection finishes at " + new BasicUtility().getCurrentTime());

                // mark the worksheet
                System.out.println("[" + Thread.currentThread().getName() + "]: Sheet " + sheet.getSheetName()
                        + " Marking starts at " + new BasicUtility().getCurrentTime());

                bu.clusterMark(stageIIClusters, sheet);
                bu.smellyCellMark(this.lockForSS, sheet, sdc.getDetectedSmellyCells());

                System.out.println("[" + Thread.currentThread().getName() + "]: Sheet " + sheet.getSheetName()
                        + " Marking finishes at " + new BasicUtility().getCurrentTime());


                //Evaluation
                staSheet.setStageIIClusters(stageIIClusters);
                staSheet.setSmellyCells(sdc.getDetectedSmellyCells());
            } else {
                System.out.println("Seed cluster does not exist.");
            }

            staSheet.calculateForDetection();
            staSheet.calculateForSmell();
            staSheet.setEndTime(System.nanoTime());

            System.out.println(Thread.currentThread().getName() +
                    ": Spreadsheet = " + staSheet.fileName + ", sheet name = " + sheet.getSheetName() +
                    ", worksheet index = " + finishedWS.incrementAndGet());

            //在这里单独输出每个有意义的被标记的worksheet
            //似乎workbook在多个线程共享的时候，进行并发的写操作，出现了问题，修改没有反应到最后的输出结果中

            //printLastSheet();
        }
        catch (RuntimeException ignored) {
            staSheet.setEndTime(System.nanoTime());
            staSheet.timeout = true;

            System.out.println("#*#*#*#*#*#*#*#*#*"+Thread.currentThread().getName() +
                    ": " + staSheet.fileName + "/" + sheet.getSheetName() + " timeouts.");
        }

    }

    public synchronized void printLastSheet() throws IOException {

        Workbook workbook = sheet.getWorkbook();
        int currentFlag = 0;
        try {
            currentFlag = printFlag.get(fileName).decrementAndGet();
            //System.out.println("fileName = " + fileName + ", CurrentCount = " + currentFlag);
        }
        catch (NullPointerException ignored) {
            //TODO：我不清楚这里为什么会报告空指针异常，先忽略吧。
            System.out.println("TestWorksheet.java line 254: NullPointerE_xception");
        }
        if (currentFlag > 0) return;

//        System.out.println("Begin to flush out.");

        String suffix = null;
        if (workbook.getSpreadsheetVersion().name().equals("EXCEL97")) {
            suffix = "xls";
        }
        else if (workbook.getSpreadsheetVersion().name().equals("EXCEL2007")) {
            suffix = "xlsx";
        }

        String prefix = fileName.substring(0, fileName.lastIndexOf('.'));
        String outFileStr = categoryDirStr + fileSeparator + prefix
                + "_" + GP.testDate + "." + suffix;

        System.out.println(outFileStr);

        FileOutputStream outFile = new FileOutputStream(outFileStr);
        System.out.println(outFileStr);
        //TODO: 不清楚为什么workbook会空指针
        if (workbook == null || outFile == null) return;
        try {
            workbook.write(outFile);
        } catch (NullPointerException ignored) {
            System.out.println("org.apache.poi.poifs.filesystem." +
                    "FilteringDirectoryNode$FilteringIterator.<init>(FilteringDirectoryNode.java:193)");
        }


        outFile.close();
        workbook.close();
        printFlag.remove(fileName);
    }
}
