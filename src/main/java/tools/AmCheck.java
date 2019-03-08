package tools;

import extraction.weakFeatureExtraction.Wrapper;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
import thirdparty.CACheck.cellarray.extract.CAResult;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static kernel.GP.fileSeparator;
import static kernel.GP.parent_dir;

/**
 * Created by Yolanda on 2017/5/8.
 */
public class AmCheck {
    public static String toolName = "AmCheck";
    public static String dataset = "VEnron2";
    public static boolean checking = false;
    public static int stepIndex = 6;

    public static void main(String args[]) throws IOException, InvalidFormatException {

        if (checking) {
            UCheck.computeDetectionResult(toolName);
            return;
        }
        runTests(toolName, 3, stepIndex);
    }

    public static void runTests(String toolName, int type, int stepIndex) throws IOException, InvalidFormatException {
        // [1+ stepWidth*stepIndex, stepWidth*(stepIndex+1) ]
        int stepWidth = 300;

        String inDirPath = parent_dir + fileSeparator + "Inputs" + fileSeparator + dataset;
        String outDirPath = parent_dir + fileSeparator + "Outputs" +
                fileSeparator + toolName + stepIndex;
        File inDir = new File(inDirPath);
        File outDir = new File(outDirPath);
        if (!outDir.exists()) outDir.mkdir();

        Workbook workbook = new HSSFWorkbook();
        Sheet sheet = workbook.createSheet("result of " + toolName);
        int rowIndex = 0, smellCount = 0;
        Row row = sheet.createRow(rowIndex++);
        row.createCell(0).setCellValue("Category");
        row.createCell(1).setCellValue("Spreadsheet");
        row.createCell(2).setCellValue("Worksheet");
        row.createCell(3).setCellValue("List of Smells");
        row.createCell(4).setCellValue("# Smells");

        ExecutorService executorService = Executors.newCachedThreadPool();


        int fileCount = 0;
        for (File category : inDir.listFiles()) {
            //if (timeBudget > 5) break;
            for (File file : category.listFiles()) {
                ++fileCount;

                if (!(fileCount > stepWidth*stepIndex && fileCount <= stepWidth*(stepIndex+1))) continue;
                System.out.println("---- fileCount = " + (fileCount - stepWidth*stepIndex));

                Workbook currentWorkbook = WorkbookFactory.create(new FileInputStream(file));
                for (Sheet currentSheet : currentWorkbook) {
                    Wrapper singletonWrapper = new Wrapper(currentSheet, type);
                    List<CAResult> caResults = singletonWrapper.processSheet(toolName);

                    if (caResults == null) continue;
                    List<String> smellList = new ArrayList<>();
                    for (CAResult ca_result: caResults) {
                        for (Cell smellyCell: ca_result.ambiguousCells) {
                            smellList.add(new CellReference(smellyCell).formatAsString());
                            smellCount++;
                        }
                    }

                    row = sheet.createRow(rowIndex++);
                    row.createCell(0).setCellValue(category.getName());
                    row.createCell(1).setCellValue(file.getName());
                    row.createCell(2).setCellValue(currentSheet.getSheetName());
                    row.createCell(3).setCellValue(smellList.toString());
                    row.createCell(4).setCellValue(smellList.size());
                }

                File category_output = new File(outDirPath + fileSeparator + category.getName());
                if (!(category_output.exists())) category_output.mkdir();

                File file_output = new File(category_output.getPath(), file.getName());
                FileOutputStream outFileStream = new FileOutputStream(file_output);
                currentWorkbook.write(outFileStream);
                outFileStream.close();
            }
        }

        System.out.println("SmellCount = " + smellCount);
        File desFile = new File(outDirPath, toolName+" results.xls");
        if (!desFile.exists()) desFile.createNewFile();
        FileOutputStream output = new FileOutputStream(desFile);
        workbook.write(output);
        output.close();
    }
}

