package DataCollection;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class DataCompletion {

    public static void main(String args[]) throws IOException, InvalidFormatException {
        String inputFN = "C:\\Users\\njuli\\Desktop\\实验结果\\大数据检测结果\\case study结果分析.xlsx";
        String outputFN = "C:\\Users\\njuli\\Desktop\\实验结果\\大数据检测结果\\case study结果分析-comp.xlsx";

        File inputF = new File(inputFN);
        Workbook workbook = WorkbookFactory.create(new FileInputStream(inputF));

        Sheet sheet = workbook.getSheet("CUSTODES");

        completeInfo(sheet);

        sheet = workbook.getSheet("WARDER");

        completeInfo(sheet);

        File outputF = new File(outputFN);
        if (!outputF.exists()) outputF.createNewFile();
        FileOutputStream outputFS = new FileOutputStream(outputF);
        workbook.write(outputFS);
        outputFS.close();

    }

    private static void completeInfo(Sheet sheet) {
        for (int j = 2; j<=7140; j++) {
            Row r = sheet.getRow(j);
            for (int i=0; i<=2; i++) {
                Cell cell = r.getCell(i);
                if (cell == null) {
                    Cell newCell = r.createCell(i);
                    Cell previousCell = sheet.getRow(r.getRowNum()-1).getCell(i);
                    if (previousCell.getCellType() == 0)
                        newCell.setCellValue(previousCell.getNumericCellValue());
                    else
                        newCell.setCellValue(previousCell.toString());
                }
            }
        }
    }
}