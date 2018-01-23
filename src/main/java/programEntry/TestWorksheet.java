package programEntry;

import clustering.bootstrappingClustering.BootstrappingClustering;
import clustering.bootstrappingClustering.FeatureCellMatrix;
import clustering.hacClustering.HacClustering;
import clustering.hacClustering.TreeEditDistance;
import clustering.smellDetectionClustering.SmellDetectionClustering;
import entity.Cluster;
import entity.InfoOfSheet;
import experiment.GroundTruthStatistics;
import experiment.StatisticsForSheet;
import featureExtraction.FeatureExtraction;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellReference;
import utility.BasicUtility;

import java.io.BufferedWriter;
import java.util.*;

import static programEntry.GP.fileSeparator;
import static programEntry.GP.finishedWS;
import static programEntry.MainClass.groundTruthPath;
import static programEntry.MainClass.numberOfFormula;

public class TestWorksheet {

    static StatisticsForSheet testWorksheet(String fileName, Sheet sheet, BufferedWriter logBuffer,
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
