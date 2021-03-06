package statistics;

import entity.Smell;
import kernel.GP;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import utility.BasicUtility;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
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

    public CopyOnWriteArrayList<StatisticsForSheet> sheetList;

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

    private String category = null;

    int[] clusterSizeGT = {0,0,0,0};
    int[] clusterSize = {0,0,0,0};

    private int index = 0;

    public StatisticsForAll() {
        sheetList = new CopyOnWriteArrayList<StatisticsForSheet>();
    }

    public synchronized void log(String prefixDir, List<String> errorExcelList)
            throws IOException {
        setEndTime(System.nanoTime());

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("result");

        // set the column width, in case manually modifying the width when I check the datum.
        sheet.setDefaultColumnWidth(12);

        Row rowHeader = sheet.createRow(0);
        rowHeader.createCell(0).setCellValue("Index");
        rowHeader.createCell(1).setCellValue("Category");
        rowHeader.createCell(2).setCellValue("SpreadSheet");
        rowHeader.createCell(3).setCellValue("Sheet");

        rowHeader.createCell(4).setCellValue("GTSmells");
        rowHeader.createCell(5).setCellValue("ToolSmells");

        rowHeader.createCell(6).setCellValue("TP");
        rowHeader.createCell(7).setCellValue("FP");
        rowHeader.createCell(8).setCellValue("FN");

        rowHeader.createCell(9).setCellValue("Precision");
        rowHeader.createCell(10).setCellValue("Recall");
        rowHeader.createCell(11).setCellValue("F-measure");

        rowHeader.createCell(13).setCellValue("GTClu, ToolClu");
        rowHeader.createCell(14).setCellValue("TP, FP, FN");
        rowHeader.createCell(15).setCellValue("P, R, F");

        rowHeader.createCell(16).setCellValue("绝对时间");
        rowHeader.createCell(17).setCellValue("CPU时间");

        rowHeader.createCell(18).setCellValue("cluster-p");
        rowHeader.createCell(19).setCellValue("cluster-R");
        rowHeader.createCell(20).setCellValue("cluster-F");

        rowHeader.createCell(21).setCellValue("Defects");


        sheetList.sort((o1, o2) -> {
            int cmp1 = o1.getSpreadsheet().compareTo(o2.getSpreadsheet());
            int cmp2 = o1.getWorksheet().compareTo(o2.getWorksheet());

            if (cmp1 != 0)
                return cmp1;

            return cmp2;
        });

        StatisticsForSheet prevSheet = null;
        int ssCount = 0;
        for (int i=0; i<=3; i++) {
            TP[i] = 0;
            FP[i] = 0;
            FN[i] = 0;
        }
        gt_clustersSize = 0;
        gt_smellSize = 0;
        stageClusterSize = 0;
        stageSmellSize = 0;

        double clusterP = 0, clusterR = 0, detectP = 0, detectR =0;
        int clusterN = 0, detectN = 0;

        for (StatisticsForSheet staSheet : sheetList) {
            //TODO:
            for (int i=0;i<4;i++) {
                clusterSize[i] += staSheet.clusterSize[i];
                clusterSizeGT[i] += staSheet.clusterSizeGT[i];
            }

            int index = sheetList.indexOf(staSheet)+1;
            Row row = sheet.createRow(index);

            if (prevSheet == null || !prevSheet.getSpreadsheet().equals(staSheet.getSpreadsheet())) {
                row.createCell(0).setCellValue(++ssCount);
                row.createCell(1).setCellValue(staSheet.category);
                this.category = staSheet.category;
                row.createCell(2).setCellValue(staSheet.getSpreadsheet());
            }

            Cell timeoutCellFlag = row.createCell(3);
            timeoutCellFlag.setCellValue(staSheet.getWorksheet());

            if (staSheet.timeout) {
                CellStyle errorStyle = sheet.getWorkbook().createCellStyle();
                Font font = sheet.getWorkbook().createFont();
                font.setFontName(XSSFFont.DEFAULT_FONT_NAME);
                font.setFontHeightInPoints((short)15);
                font.setColor(IndexedColors.DARK_RED.getIndex());
                errorStyle.setFont(font);
                timeoutCellFlag.setCellStyle(errorStyle);
            }

            //if (staSheet.virtual) continue;

            row.createCell(4).setCellValue(staSheet.getGt_smellList().size());
            row.createCell(5).setCellValue(staSheet.getSmellyCells().size());

            row.createCell(6).setCellValue(staSheet.TP[1]);
            row.createCell(7).setCellValue(staSheet.FP[1]);
            row.createCell(8).setCellValue(staSheet.FN[1]);

            row.createCell(9).setCellValue(roundDouble(staSheet.precision[1]));
            row.createCell(10).setCellValue(roundDouble(staSheet.recall[1]));
            row.createCell(11).setCellValue(roundDouble(staSheet.fMeasure[1]));

//            printCellRefsForSmell(staSheet.getGt_smellList(), row.createCell(10));
//            printCellRefsForSmell(staSheet.getStageCRs(), row.createCell(11));
            row.createCell(12).setCellValue(staSheet.getStageIIClusters().size());
            row.createCell(13).setCellValue("("+ staSheet.getGt_clusterList().size() +",  "+
                    staSheet.getStageIIClusters().size() +")");
            row.createCell(14).setCellValue("("+ staSheet.TP[0] +",  "+ staSheet.FP[0] +",  "+ staSheet.FN[0] +")");
            row.createCell(15).setCellValue("("+ roundDouble(staSheet.precision[0]) +", "+
                    roundDouble(staSheet.recall[0]) +", "+ roundDouble(staSheet.fMeasure[0]) +")");

            //row.createCell(16).setCellValue(consumedTime);
            row.createCell(17).setCellValue(staSheet.cpuTime);

            row.createCell(18).setCellValue(roundDouble(staSheet.precision[0]));
            row.createCell(19).setCellValue(roundDouble(staSheet.recall[0]));
            row.createCell(20).setCellValue(roundDouble(staSheet.fMeasure[0]));

            List<Smell> defectList = staSheet.getSmellyCells();
            List<String> smellList = new ArrayList<>();
            for (Smell smell : defectList)
                    smellList.add(smell.getCr().formatAsString());
            String smellString = smellList.toString();
            int defectIndex = 20;
            for (int i=0; i*1000 < smellString.length(); i++) {
                row.createCell(defectIndex++).setCellValue(
                        smellString.substring(i*1000, Math.min((i+1)*1000, smellString.length())));
            }

            if (staSheet.precision[0] + staSheet.recall[0] > 0) {
                clusterN++;
                clusterP += staSheet.precision[0];
                clusterR += staSheet.recall[0];
            }
            if (staSheet.precision[1] + staSheet.recall[1] > 0) {
                detectN++;
                detectP += staSheet.precision[1];
                detectR += staSheet.recall[1];
            }

            gt_clustersSize += staSheet.getGt_clusterList().size();
            gt_smellSize += staSheet.getGt_smellList().size();
            stageClusterSize += staSheet.getStageIIClusters().size();
            stageSmellSize += staSheet.getSmellyCells().size();

            for (int i=0;i<=3;i++) {
                TP[i] += staSheet.TP[i];
                FP[i] += staSheet.FP[i];
                FN[i] += staSheet.FN[i];
            }

            prevSheet = staSheet;
        }

        for (int i=0; i<=3; i++)
            calculateForPercentage(i);

        Row rowTailor = sheet.createRow(sheetList.size()+1);
        rowTailor.createCell(0).setCellValue("TOTAL");

        rowTailor.createCell(4).setCellValue(gt_smellSize);
        rowTailor.createCell(5).setCellValue(stageSmellSize);
        rowTailor.createCell(6).setCellValue(TP[1]);
        rowTailor.createCell(7).setCellValue(FP[1]);
        rowTailor.createCell(8).setCellValue(FN[1]);

        if (!GP.buildArtifact)
            System.out.println("TP = " + TP[1] + ", FP = " + FP[1] + ", FN = " + FN[1]);
        rowTailor.createCell(9).setCellValue(roundDouble(precision[1]));
        rowTailor.createCell(10).setCellValue(roundDouble(recall[1]));
        rowTailor.createCell(11).setCellValue(roundDouble(fMeasure[1]));

        rowTailor.createCell(13).setCellValue("("+ gt_clustersSize +", "+ stageClusterSize +")");
        rowTailor.createCell(14).setCellValue("("+ TP[0] +", "+ FP[0] +", "+ FN[0] +")");
        rowTailor.createCell(15).setCellValue("("+ roundDouble(precision[0]) +", "+ roundDouble(recall[0])
                +", "+ roundDouble(fMeasure[0]) +")");

        rowTailor.createCell(16).setCellValue((endTime-beginTime)/1000_000_000);

        rowTailor.createCell(18).setCellValue(roundDouble(precision[0]));
        rowTailor.createCell(19).setCellValue(roundDouble(recall[0]));
        rowTailor.createCell(20).setCellValue(roundDouble(fMeasure[0]));

        String fileName;
        if (GP.buildArtifact)
            fileName = GP.parent_dir + GP.fileSeparator + "clustering and detection results.xlsx";
        else
            fileName = prefixDir + GP.fileSeparator + category +"_"+ ssCount +"(" + new BasicUtility().getCurrentTime() + ").xlsx";

        out.println(fileName);
        FileOutputStream resultStream = new FileOutputStream(new File(fileName));
        workbook.write(resultStream);
        resultStream.close();
//        out.println("log finishes and middle = " + middleFlag + ".");

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

    public synchronized boolean add(StatisticsForSheet staSheet, BufferedWriter logBuffer) {
//        if (!staSheet.virtual &&
//                staSheet.getGt_clusterList().size()==0 &&
//                staSheet.getGt_smellList().size() == 0 &&
//                staSheet.getStageIIClusters().size() == 0 &&
//                staSheet.getSmellyCells().size() == 0){
//            if (!staSheet.timeout) {
//                return false;
//            }
//        }

        sheetList.add(staSheet);

        if (sheetList.size() != 0 && sheetList.size() % 100 == 0) {
            try {
                log(GP.prefixOutDir, null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    public void getInfo() {
        System.err.printf("# Identified clusters = %d, # Detected smells = %d\n", stageClusterSize, stageSmellSize);
    }
}
