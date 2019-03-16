package DataCollection;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class NonTimeoutOrCrash {

    public static void main(String args[]) throws IOException, InvalidFormatException {
        String inputFN = "C:\\Users\\njuli\\Desktop\\实验结果\\大数据检测结果\\case study结果分析-comp.xlsx";
        String outputFN = "C:\\Users\\njuli\\Desktop\\实验结果\\大数据检测结果\\case study结果分析-nonTC.xlsx";

        File inputF = new File(inputFN);
        Workbook workbook = WorkbookFactory.create(new FileInputStream(inputF));


        Workbook outputWB = new XSSFWorkbook();
        Sheet outputSheet = outputWB.createSheet("non-TC VEnron2");
        Row rowHeader = outputSheet.createRow(0);
        rowHeader.createCell(0).setCellValue("Category");
        rowHeader.createCell(1).setCellValue("SpreadSheet");
        rowHeader.createCell(2).setCellValue("Sheet");
        int outputRowIndex = 1;

        Sheet enronSheet = workbook.getSheet("VEnron2");
        Sheet amTCSheet = workbook.getSheet("Am timeout");
        Sheet caTCSheet = workbook.getSheet("CA timeout");
        Sheet cuTCSheet = workbook.getSheet("CU timeout");
        Sheet waTCSheet = workbook.getSheet("WA timeout");

        for (int j = 1; j<=7140; j++) {
            Row enronRow = enronSheet.getRow(j);
            double enronCategory = enronRow.getCell(0).getNumericCellValue();
            String enronSSName = enronRow.getCell(1).getStringCellValue();
            String enronWSName = enronRow.getCell(2).getStringCellValue();

            // checking in four TC lists
            if (checkingInTCList(amTCSheet, enronSSName, enronWSName) ||
            checkingInTCList(caTCSheet, enronSSName, enronWSName) ||
            checkingInTCList(cuTCSheet, enronSSName, enronWSName) ||
            checkingInTCList(waTCSheet, enronSSName, enronWSName)) {
                continue;
            }

            Row row = outputSheet.createRow(outputRowIndex);
            row.createCell(0).setCellValue(enronCategory);
            row.createCell(1).setCellValue(enronSSName);
            row.createCell(2).setCellValue(enronWSName);
            outputRowIndex++;
        }

        FileOutputStream resultStream = new FileOutputStream(new File(outputFN));
        outputWB.write(resultStream);
        resultStream.close();
    }

    private static boolean checkingInTCList(Sheet caTCSheet, String enronSSName, String enronWSName) {
        boolean TC = false;
        for (Row row: caTCSheet) {
            String SSName = row.getCell(1).getStringCellValue();
            String WSName = row.getCell(2).getStringCellValue();
            if (enronSSName.equals(SSName) && enronWSName.equals(WSName))
                TC = true;
        }
        return TC;
    }
}
