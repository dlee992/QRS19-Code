package tools;

import DataCollection.FilterSet;
import extraction.weakFeatureExtraction.Wrapper;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
import thirdparty.CACheck.cellarray.extract.CAResult;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static kernel.GP.fileSeparator;
import static kernel.GP.parent_dir;

/**
 * Created by Yolanda on 2017/5/8.
 */
public class AmCheck {
    public static String toolName = "AmCheck";
    public static String dataset = "VEnron2-Clean";
    public static boolean checking = false;
    public static int stepIndex = 0;
    //public static int restart = 1129;

    public static void main(String args[]) throws IOException, InvalidFormatException {

        if (checking) {
            UCheck.computeDetectionResult(toolName);
            return;
        }
        runTests(toolName, 3, stepIndex);
    }

    public static void runTests(String toolName, int type, int stepIndex) throws IOException, InvalidFormatException {
        // [1+ stepWidth*stepIndex, stepWidth*(stepIndex+1) ]
        int stepWidth = Integer.MAX_VALUE;

        String inDirPath = parent_dir + fileSeparator + "Inputs" + fileSeparator + dataset;
        String outDirPath = parent_dir + fileSeparator + "Outputs" + fileSeparator + toolName;
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
        row.createCell(6).setCellValue("List of Smells");
        row.createCell(4).setCellValue("# Smells");
        row.createCell(5).setCellValue("Exception Type");
        row.createCell(3).setCellValue("ConsumedTime/s");

        FilterSet filterSet = new FilterSet();

        int fileCount = 0;
        for (File category : inDir.listFiles()) {
            for (File file : category.listFiles()) {
                ++fileCount;

                if (!(fileCount > stepWidth*stepIndex && fileCount <= stepWidth*(stepIndex+1))) continue;
                System.out.println("---- fileCount = " + (fileCount - stepWidth*stepIndex) + "/ filename = " + file.getName());

                // restart from breakpoint
                //System.out.println((fileCount - stepWidth*stepIndex) + " < " + CACheck.restart);
                if (fileCount < CACheck.restart) continue;


                Workbook currentWorkbook = WorkbookFactory.create(new FileInputStream(file));

                for (Sheet currentSheet : currentWorkbook) {
                    try {
                        String hashValue = category.getName() + "/" + file.getName() + "/" + currentSheet.getSheetName();
                        if (!filterSet.set.contains(hashValue)) continue;

                        Instant beginTime = Instant.now();
                        Wrapper singletonWrapper = new Wrapper(currentSheet, type);
                        List<CAResult> caResults = singletonWrapper.processSheet(toolName);

                        List<String> smellList = new ArrayList<>();
                        if (caResults != null) {
                            for (CAResult ca_result : caResults) {
                                for (Cell smellyCell : ca_result.ambiguousCells) {
                                    smellList.add(new CellReference(smellyCell).formatAsString());
                                    smellCount++;
                                }
                            }
                        }

                        Instant endTime = Instant.now();
                        row = sheet.createRow(rowIndex++);
                        row.createCell(0).setCellValue(category.getName());
                        row.createCell(1).setCellValue(file.getName());
                        row.createCell(2).setCellValue(currentSheet.getSheetName());
                        row.createCell(4).setCellValue(smellList.size());
                        row.createCell(3).setCellValue(Duration.between(beginTime, endTime).getSeconds());

                        String smellString = smellList.toString();
                        int index = 6;
                        for (int i = 0; i * 1000 < smellString.length(); i++) {
                            row.createCell(index++).setCellValue(
                                    smellString.substring(i * 1000, Math.min((i + 1) * 1000, smellString.length())));
                        }

                        //System.out.println("SmellCount = " + smellCount);
                        File desFile = new File(outDirPath, toolName + "-" + fileCount + " results.xls");
                        if (!desFile.exists()) desFile.createNewFile();
                        FileOutputStream output = new FileOutputStream(desFile);
                        workbook.write(output);
                        output.close();


                    } catch (IllegalStateException illegalState) {
                        System.err.println("IllegalStateException in " + file.getName() + "/" + currentSheet.getSheetName());
                        row.createCell(5).setCellValue("illegalState");
                    } catch (IllegalArgumentException illegalArgument) {
                        System.err.println("illegalArgumentException in " + file.getName() + "/" + currentSheet.getSheetName());
                        row.createCell(5).setCellValue("illegalArgument");
                    } catch (ArrayIndexOutOfBoundsException arrayIndexOut) {
                        System.err.println("ArrayIndexOutOfBoundsException in " + file.getName() + "/" + currentSheet.getSheetName());
                        row.createCell(5).setCellValue("ArrayIndexOutOfBounds");
                    }

                }


                try {
                    File category_output = new File(outDirPath + fileSeparator + category.getName());
                    if (!(category_output.exists())) category_output.mkdir();

                    File file_output = new File(category_output.getPath(), file.getName());
                    FileOutputStream outFileStream = new FileOutputStream(file_output);
                    currentWorkbook.write(outFileStream);
                    outFileStream.close();
                } catch (IllegalStateException illegalState) {

                }
            }
        }

        System.out.println("SmellCount = " + smellCount);
        File desFile = new File(outDirPath, toolName+stepIndex+" results.xls");
        if (!desFile.exists()) desFile.createNewFile();
        FileOutputStream output = new FileOutputStream(desFile);
        workbook.write(output);
        output.close();
    }
}

