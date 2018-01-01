package experiment;

import clustering.smellDetectionClustering.Smell;
import entity.Cluster;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellReference;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nju-lida on 16-6-28.
 */
public class StatisticsForSheet {

    private long beginTime;
    private long endTime;

    String category;

    private List<Cluster> gt_clusterList = new ArrayList<>();
    private List<CellReference> gt_smellList = new ArrayList<>();

    private List<Cluster> stageIIClusters = new ArrayList<Cluster>();
    private List<Smell> smellyCells = new ArrayList<Smell>();
    private List<CellReference> stageCRs;

    //TODO: 0:clustering 1:smell 2:number smell 3:formula smell
    int[] TP = {0,0,0,0};
    int[] FN = {0,0,0,0};
    int[] FP = {0,0,0,0};

    double[] precision = new double[4];
    double[] recall = new double[4];
    double[] fMeasure = new double[4];

    private String spreadsheet;
    private String worksheet;

    private Sheet sheet;

    int[] clusterSizeGT = {0,0,0,0};
    int[] clusterSize = {0,0,0,0};

    public StatisticsForSheet(Sheet sheet, String category) {
        this.sheet = sheet;
        this.category =category;
    }


    public void calculateForDetection() {

        //construct the pair from the identical cluster from gt;

        for (Cluster gt_cluster : gt_clusterList) {
            gt_cluster.extractCellRefs(gt_cluster, 2);
            List<CellReference> gt_cellRefs = gt_cluster.getClusterCellRefs();

            //TODO:
            if (gt_cellRefs.size() <= 3) clusterSizeGT[0]++;
            else if (gt_cellRefs.size() <=10) clusterSizeGT[1]++;
            else if (gt_cellRefs.size() <= 100) clusterSizeGT[2]++;
            else clusterSizeGT[3]++;

            for (int i = 0; i<gt_cellRefs.size(); i++) {
                int i_index = searchInOtherClusters(gt_cellRefs.get(i), stageIIClusters);

                for (int j = i+1; j<gt_cellRefs.size(); j++) {
                    int j_index = searchInOtherClusters(gt_cellRefs.get(j), stageIIClusters);

                    if (i_index == j_index && i_index > -1) TP[0]++;
                    else FN[0]++;
                }
            }
        }

        //construct the pair from the identical cluster from stageII;
        for (Cluster stage_cluster: stageIIClusters) {
            List<CellReference> stage_cellRefs = stage_cluster.extractCellRefs(stage_cluster,2);

            //TODO:
            if (stage_cellRefs.size() <=3) clusterSize[0]++;
            else if (stage_cellRefs.size() <=10) clusterSize[1]++;
            else if (stage_cellRefs.size() <=100) clusterSize[2]++;
            else clusterSize[3]++;

            for (int i=0; i<stage_cellRefs.size(); i++) {
                int i_index = searchInOtherClusters(stage_cellRefs.get(i), gt_clusterList);

                for (int j=i+1; j<stage_cellRefs.size(); j++) {
                    int j_index = searchInOtherClusters(stage_cellRefs.get(j), gt_clusterList);

                    if (i_index == -1 || j_index == -1 || i_index != j_index)
                        FP[0] ++;
                }
            }
        }

        //
        calculateForPercentage(0);

    }

    private void calculateForPercentage(int index) {
        if (TP[index]+FP[index] == 0) precision[index] = 0;
        else precision[index] = 1.0*TP[index]/(TP[index]+FP[index]);

        if (TP[index]+FN[index] == 0) recall[index] = 0;
        else recall[index] = 1.0*TP[index]/(TP[index]+FN[index]);

        if (precision[index]+recall[index] == 0) fMeasure[index] = 0;
        else fMeasure[index] = 2*precision[index]*recall[index]/(precision[index]+recall[index]);
    }

    private int searchInOtherClusters(CellReference originCR, List<Cluster> clusters) {
        for (Cluster cluster : clusters)
            if (cluster.getClusterCellRefs().contains(originCR))
                return clusters.indexOf(cluster);
        return -1;
    }

    public void calculateForSmell() {
        stageCRs = new ArrayList<CellReference>();
        for (Smell smell : smellyCells) {
            stageCRs.add(smell.getCr());
        }

        //match groundTruthSmell with stageSmell;
        for (CellReference gt_cr : gt_smellList) {
            if (stageCRs.contains(gt_cr)) TP[1]++;
            else FN[1]++;
        }

        //reverse the match order;
        for (CellReference stage_cr : stageCRs) {
            if (!gt_smellList.contains(stage_cr)) FP[1]++;
        }

        //
        calculateForPercentage(1);

        //TODO:
        List<CellReference> dataCellGT = new ArrayList<>(), formulaCellGT = new ArrayList<>();
        for (CellReference cellR: gt_smellList) {
            int row = cellR.getRow();
            int col = cellR.getCol();
            Row sheetRow = sheet.getRow(row);
            if (sheetRow == null) continue;
            Cell cell = sheetRow.getCell(col);
            if (cell == null) continue;

            if (cell.getCellType() == 0)
                dataCellGT.add(cellR);
            if (cell.getCellType() == 2)
                formulaCellGT.add(cellR);
        }
        List<CellReference> dataCell = new ArrayList<>(), formulaCell = new ArrayList<>();
        for (Smell smell: smellyCells) {
            CellReference cellR = smell.getCr();
            int row = cellR.getRow();
            int col = cellR.getCol();
            Row sheetRow = sheet.getRow(row);
            if (sheetRow == null) continue;
            Cell cell = sheetRow.getCell(col);
            if (cell == null) continue;

            if (cell.getCellType() == 0)
                dataCell.add(cellR);
            if (cell.getCellType() == 2)
                formulaCell.add(cellR);
        }

        for (CellReference cellRGT : dataCellGT) {
            if (dataCell.contains(cellRGT)) TP[2]++;
            else FN[2]++;
        }
        for (CellReference cellR: dataCell) {
            if (!dataCellGT.contains(cellR)) FP[2]++;
        }
        calculateForPercentage(2);

        for (CellReference cellRGT : formulaCellGT) {
            if (formulaCell.contains(cellRGT)) TP[3]++;
            else FN[3]++;
        }
        for (CellReference cellR: formulaCell) {
            if (!formulaCellGT.contains(cellR)) FP[3]++;
        }
        calculateForPercentage(3);
    }

    long getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(long beginTime) {
        this.beginTime = beginTime;
    }

    long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    List<Cluster> getGt_clusterList() {
        if (gt_clusterList == null)
            gt_clusterList = new ArrayList<Cluster>();
        return gt_clusterList;
    }

    public void setGt_clusterList(List<Cluster> gt_clusterList) {
        this.gt_clusterList = gt_clusterList;
    }

    List<CellReference> getGt_smellList() {
        if (gt_smellList == null)
            gt_smellList = new ArrayList<CellReference>();
        return gt_smellList;
    }

    public void setGt_smellList(List<CellReference> gt_smellList) {
        this.gt_smellList = gt_smellList;
    }

    List<Cluster> getStageIIClusters() {
        if (stageIIClusters == null)
            stageIIClusters = new ArrayList<Cluster>();
        return stageIIClusters;
    }

    public void setStageIIClusters(List<Cluster> stageIIClusters) {
        this.stageIIClusters = stageIIClusters;
    }

    List<Smell> getSmellyCells() {
        if (smellyCells == null)
            smellyCells = new ArrayList<Smell>();
        return smellyCells;
    }

    public void setSmellyCells(List<Smell> smellyCells) {
        this.smellyCells = smellyCells;
    }


    String getSpreadsheet() {
        return spreadsheet;
    }

    public void setSpreadsheet(String spreadsheet) {
        this.spreadsheet = spreadsheet;
    }

    String getWorksheet() {
        return worksheet;
    }

    public void setWorksheet(String worksheet) {
        this.worksheet = worksheet;
    }

    List<CellReference> getStageCRs() {
        if (stageCRs == null)
            stageCRs = new ArrayList<CellReference>();
        return stageCRs;
    }

}
