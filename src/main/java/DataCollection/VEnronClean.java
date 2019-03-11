package DataCollection;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import static kernel.GP.fileSeparator;
import static kernel.GP.parent_dir;

public class VEnronClean {
    public static String dataset = "VEnron2-Clean";

    public static void main(String args[]) throws IOException, InvalidFormatException {
        String inDirPath = parent_dir + fileSeparator + "Inputs" + fileSeparator + dataset;
        File datasetDir = new File(inDirPath);

        Workbook workbook = new HSSFWorkbook();
        Sheet sheet = workbook.createSheet("result of " + "GroundTruth");
        int rowIndex = 0;
        Row row = sheet.createRow(rowIndex++);
        row.createCell(0).setCellValue("Category");
        row.createCell(1).setCellValue("Spreadsheet");
        row.createCell(2).setCellValue("Worksheet");
        row.createCell(3).setCellValue("# non-empty cells");
        row.createCell(4).setCellValue("# formula and data cells");
        row.createCell(5).setCellValue("# formula cells");
        row.createCell(6).setCellValue("ExcelFileError");
        row.createCell(7).setCellValue("CellError");

        int spreadsheetCount = 0, sheetCount = 0, formulaCount = 0, dataCount = 0, nonEmptyCount = 0;
        for (File subDir: datasetDir.listFiles()) {
            for (File excelFile : subDir.listFiles()) {
                try {
                    Workbook workbookNow = WorkbookFactory.create(new FileInputStream(excelFile));
                    spreadsheetCount++;
                    int sheetCountNow = 0;
                    for (Sheet sheetNow : workbookNow) {
                        try {
                            sheetCountNow++;
                            System.out.println(subDir.getName() + "/" + excelFile.getName() + "/" + sheetNow.getSheetName());

                            int dataCountNow = 0, formulaCountNow = 0, nonEmptyCountNow = 0;
                            for (Row rowNow : sheetNow) {
                                for (Cell cell : rowNow) {
                                    nonEmptyCountNow++;
                                    if (cell.getCellType() == 0) {
                                        cell.getNumericCellValue();
                                        //System.out.print(cell.getNumericCellValue());
                                        dataCountNow++;
                                    } else if (cell.getCellType() == 2) {
                                        cell.getCellFormula();
                                        //System.out.print(cell.getCellFormula());
                                        formulaCountNow++;
                                    }
                                }
                            }

                            // dump current sheet information

                            row = sheet.createRow(rowIndex++);
                            row.createCell(0).setCellValue(subDir.getName());
                            row.createCell(1).setCellValue(excelFile.getName());
                            row.createCell(2).setCellValue(sheetNow.getSheetName());
                            row.createCell(3).setCellValue(nonEmptyCountNow);
                            row.createCell(4).setCellValue(dataCountNow + formulaCountNow);
                            row.createCell(5).setCellValue(formulaCountNow);

                            dataCount += dataCountNow;
                            formulaCount += formulaCountNow;
                            nonEmptyCount += nonEmptyCountNow;

                        } catch (Exception e) {
                            System.err.println("cellError: "+subDir.getName() + "/" + excelFile.getName() + "/" + sheetNow.getSheetName());

                            row = sheet.createRow(rowIndex++);
                            row.createCell(0).setCellValue(subDir.getName());
                            row.createCell(1).setCellValue(excelFile.getName());
                            row.createCell(2).setCellValue(sheetNow.getSheetName());
                            row.createCell(7).setCellValue(1);
                        }
                    }
                    sheetCount += sheetCountNow;
                } catch (Exception e) {
                    System.err.println("excelFileError: "+subDir.getName() + "/" + excelFile.getName());
                    row = sheet.createRow(rowIndex++);
                    row.createCell(0).setCellValue(subDir.getName());
                    row.createCell(1).setCellValue(excelFile.getName());
                    row.createCell(6).setCellValue(1);
                }
            }
        }

        row = sheet.createRow(rowIndex++);
        row.createCell(0).setCellValue("TOTAL");
        row.createCell(1).setCellValue(spreadsheetCount);
        row.createCell(2).setCellValue(sheetCount);
        row.createCell(3).setCellValue(nonEmptyCount);
        row.createCell(4).setCellValue(dataCount+formulaCount);
        row.createCell(5).setCellValue(formulaCount);

        System.out.println("done.");
        File desFile = new File(parent_dir+fileSeparator+"Inputs", "VEnron2 Clean collection.xls");
        if (!desFile.exists()) desFile.createNewFile();
        FileOutputStream output = new FileOutputStream(desFile);
        workbook.write(output);
        output.close();
    }

}
