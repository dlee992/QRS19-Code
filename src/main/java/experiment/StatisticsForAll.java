package experiment;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import utility.BasicUtility;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.lang.System.out;

/**
 * Created by nju-lida on 16-6-28.
 */
public class StatisticsForAll {
    // Returns a synchronized (thread-safe) list backed by the specified list. In order to guarantee serial access,
    // it is critical that all access to the backing list is accomplished through the returned list.
    private static Logger logger = LogManager.getLogger(StatisticsForAll.class.getName());

    private CopyOnWriteArrayList<StatisticsForSheet> sheetList;

    private long beginTime;
    private long endTime;

    private int[] TP = {0,0,0,0};
    private int[] FN = {0,0,0,0};
    private int[] FP = {0,0,0,0};

    private double[] precision = new double[4];
    private double[] recall = new double[4];
    private double[] fMeasure = new double[4];

    private int gt_clustersSize = 0;
    private int stageClusterSize = 0;
    private int gt_smellSize = 0;
    private int stageSmellSize = 0;

    int[] clusterSizeGT = {0,0,0,0};
    int[] clusterSize = {0,0,0,0};

    private int index = 0;

    public StatisticsForAll() {
        sheetList = new CopyOnWriteArrayList<StatisticsForSheet>();
    }

    public void log(String fileNam) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("result");

        // set the column width, in case manually modifying the width when I check the datum.
        sheet.setDefaultColumnWidth(15);

        Row rowHeader = sheet.createRow(0);
        rowHeader.createCell(0).setCellValue("Index");
        rowHeader.createCell(1).setCellValue("SpreadSheet");

        rowHeader.createCell(2).setCellValue("Sheet");
        rowHeader.createCell(3).setCellValue("(GTClusters, MyClusters)");
        rowHeader.createCell(4).setCellValue("(TP,FP,FN) for Cluster");
        rowHeader.createCell(6).setCellValue("(GTSmellyCells, MySmellyCells)");
        rowHeader.createCell(7).setCellValue("(TP,FP,FN) for Smell");
        rowHeader.createCell(5).setCellValue("(Precision,Recall,FMeasure) for Clustering");
        rowHeader.createCell(8).setCellValue("(Precision,Recall,FMeasure) for Smell Detection");
        rowHeader.createCell(9).setCellValue("Consumed Time/s");
        rowHeader.createCell(10).setCellValue("Ground Truth");
        rowHeader.createCell(11).setCellValue("My Smells");

        rowHeader.createCell(12).setCellValue("(TP,FP,FN) number");
        rowHeader.createCell(13).setCellValue("(Precision,Recall,FMeasure) number");
        rowHeader.createCell(14).setCellValue("(TP,FP,FN) formula");
        rowHeader.createCell(15).setCellValue("(Precision,Recall,FMeasure) formula");

        for (StatisticsForSheet staSheet : sheetList) {
            //TODO:
            for (int i=0;i<4;i++) {
                clusterSize[i] += staSheet.clusterSize[i];
                clusterSizeGT[i] += staSheet.clusterSizeGT[i];
            }

            int index = sheetList.indexOf(staSheet)+1;
            Row row = sheet.createRow(index);
            row.createCell(0).setCellValue(index);
            row.createCell(1).setCellValue(staSheet.getSpreadsheet());
            row.createCell(2).setCellValue(staSheet.getWorksheet());
            row.createCell(3).setCellValue("("+ staSheet.getGt_clusterList().size() +",  "+ staSheet.getStageIIClusters().size() +")");
            row.createCell(4).setCellValue("("+ staSheet.TP[0] +",  "+ staSheet.FP[0] +",  "+ staSheet.FN[0] +")");
            row.createCell(6).setCellValue("("+ staSheet.getGt_smellList().size() +", "+ staSheet.getSmellyCells().size() +")");
            row.createCell(7).setCellValue("("+ staSheet.TP[1] +",  "+ staSheet.FP[1] +",  "+ staSheet.FN[1] +")");
            row.createCell(5).setCellValue("("+ roundDouble(staSheet.precision[0]) +", "+ roundDouble(staSheet.recall[0]) +", "+ roundDouble(staSheet.fMeasure[0]) +")");
            row.createCell(8).setCellValue("("+ roundDouble(staSheet.precision[1]) +", "+ roundDouble(staSheet.recall[1]) +", "+ roundDouble(staSheet.fMeasure[1]) +")");
            row.createCell(9).setCellValue((staSheet.getEndTime() - staSheet.getBeginTime())/1000);

            printCellRefsForSmell(staSheet.getGt_smellList(), row.createCell(10));
            printCellRefsForSmell(staSheet.getStageCRs(), row.createCell(11));

            row.createCell(12).setCellValue("("+ staSheet.TP[2] +",  "+ staSheet.FP[2] +",  "+ staSheet.FN[2] +")");

            row.createCell(14).setCellValue("("+ staSheet.TP[3] +",  "+ staSheet.FP[3] +",  "+ staSheet.FN[3] +")");

            gt_clustersSize += staSheet.getGt_clusterList().size();
            gt_smellSize += staSheet.getGt_smellList().size();
            stageClusterSize += staSheet.getStageIIClusters().size();
            stageSmellSize += staSheet.getSmellyCells().size();

            for (int i=0;i<=3;i++) {
                TP[i] += staSheet.TP[i];
                FP[i] += staSheet.FP[i];
                FN[i] += staSheet.FN[i];
            }
        }

        for (int i=0; i<=3; i++)
            calculateForPercentage(i);

        Row rowTailor = sheet.createRow(sheetList.size()+1);
        rowTailor.createCell(0).setCellValue("Total");
        rowTailor.createCell(3).setCellValue("("+ gt_clustersSize +", "+ stageClusterSize +")");
        rowTailor.createCell(4).setCellValue("("+ TP[0] +", "+ FP[0] +", "+ FN[0] +")");
        rowTailor.createCell(6).setCellValue("("+ gt_smellSize +", "+ stageSmellSize +")");
        rowTailor.createCell(7).setCellValue("("+ TP[1] +", "+ FP[1] +", "+ FN[1] +")");
        System.out.println("TP = " + TP[1] + ", FP = " + FP[1] + ", FN = " + FN[1]);
        rowTailor.createCell(5).setCellValue("("+ roundDouble(precision[0]) +", "+ roundDouble(recall[0]) +", "+ roundDouble(fMeasure[0]) +")");
        rowTailor.createCell(8).setCellValue("("+ roundDouble(precision[1]) +", "+ roundDouble(recall[1]) +", "+ roundDouble(fMeasure[1]) +")");
        rowTailor.createCell(9).setCellValue((endTime-beginTime)/1000);

        rowTailor.createCell(12).setCellValue("("+ TP[2] +", "+ FP[2] +", "+ FN[2] +")");
        rowTailor.createCell(14).setCellValue("("+ TP[3] +", "+ FP[3] +", "+ FN[3] +")");
        rowTailor.createCell(13).setCellValue("("+ roundDouble(precision[2]) +", "+ roundDouble(recall[2]) +", "+ roundDouble(fMeasure[2]) +")");
        rowTailor.createCell(15).setCellValue("("+ roundDouble(precision[3]) +", "+ roundDouble(recall[3]) +", "+ roundDouble(fMeasure[3]) +")");

        String fileName =new BasicUtility().getCurrentTime()+".xlsx";
        out.println(fileName);
        FileOutputStream resultStream = new FileOutputStream(new File(fileNam + fileName));
        workbook.write(resultStream);
        resultStream.close();

//        out.printf("clusterSize <=3: %d, <=10: %d, <=100: %d, >100: %d\n",
//                clusterSize[0], clusterSize[1], clusterSize[2], clusterSize[3]);
//        out.printf("clusterSizeGT <=3: %d, <=10: %d, <=100: %d, >100: %d\n",
//                clusterSizeGT[0], clusterSizeGT[1], clusterSizeGT[2], clusterSizeGT[3]);
    }

    private String roundDouble(double v) {
        return String.format("%.3f",v);
    }

    private void printCellRefsForSmell(List<CellReference> smellCRs, Cell cell) {
        smellCRs.sort(Comparator.comparing(CellReference::formatAsString));

        StringBuilder tmpList = new StringBuilder();
        for (CellReference cr : smellCRs) {
            tmpList.append(cr.formatAsString());
            if (smellCRs.indexOf(cr) != smellCRs.size()-1)
                tmpList.append(",");
        }
        cell.setCellValue(tmpList.toString());
    }

    private void calculateForPercentage(int index) {
        if (TP[index]+FP[index] == 0) precision[index] = 0;
        else precision[index] = 1.0*TP[index]/(TP[index]+FP[index]);

        if (TP[index]+FN[index] == 0) recall[index] = 0;
        else recall[index] = 1.0*TP[index]/(TP[index]+FN[index]);

        if (precision[index]+recall[index] == 0) fMeasure[index] = 0;
        else fMeasure[index] = 2*precision[index]*recall[index]/(precision[index]+recall[index]);
    }

    public void setBeginTime(long beginTime) {
        this.beginTime = beginTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public synchronized void add(StatisticsForSheet staSheet) {
        if (staSheet.getGt_clusterList().size()==0 &&
                staSheet.getGt_smellList().size() == 0 &&
                staSheet.getStageIIClusters().size() == 0 &&
                staSheet.getSmellyCells().size() == 0) return;
        sheetList.add(staSheet);
        logger.debug("index=" + (++index) + ": "+ staSheet.getWorksheet()+" --OF-- " +staSheet.getSpreadsheet());
    }
}
