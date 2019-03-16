package DataCollection;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FilterInEachList {

    public static void main(String args[]) throws IOException, InvalidFormatException {
        String inputFN = "C:\\Users\\njuli\\Desktop\\实验结果\\大数据检测结果\\case study non-timeout results.xlsx";
        String outputFN = "C:\\Users\\njuli\\Desktop\\实验结果\\大数据检测结果\\UnionSet.xlsx";

        File inputF = new File(inputFN);
        Workbook workbook = WorkbookFactory.create(new FileInputStream(inputF));


        Sheet nonTCSheet = workbook.getSheet("non-TC VEnron2");
        Sheet amSheet = workbook.getSheet("AmCheck (2)");
        Sheet caSheet = workbook.getSheet("CACheck (2)");
        Sheet cuSheet = workbook.getSheet("CUSTODES (2)");
        Sheet waSheet = workbook.getSheet("WARDER (2)");

        existOrNot(nonTCSheet, amSheet);
        existOrNot(nonTCSheet, caSheet);
        existOrNot(nonTCSheet, cuSheet);
        existOrNot(nonTCSheet, waSheet);

        FileOutputStream resultStream = new FileOutputStream(new File(outputFN));
        workbook.write(resultStream);
        resultStream.close();
    }

    private static void existOrNot(Sheet nonTCSheet, Sheet waSheet) {
        for (Row row: waSheet) {
            if (row.getRowNum() == 0) continue;
            Cell nonLastCell =  row.getCell(1);
            if (nonLastCell == null) continue;
            String SSName = row.getCell(1).getStringCellValue();
            String WSName = row.getCell(2).getStringCellValue();

            boolean exist = false;
            for (Row nonTCRow: nonTCSheet) {
                String nonTCSSName = nonTCRow.getCell(1).getStringCellValue();
                String nonTCWSName = nonTCRow.getCell(2).getStringCellValue();
                if (nonTCSSName.equals(SSName) && nonTCWSName.equals(WSName)) {
                    exist = true;
                    Cell nonTCCell = nonTCRow.createCell(3);
                    nonTCCell.setCellValue("union");
                }
            }

            Cell cell = row.createCell(6);
            if (exist)
                cell.setCellValue("save");
            else
                cell.setCellValue("delete");
        }
    }
}
