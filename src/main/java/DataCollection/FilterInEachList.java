package DataCollection;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class FilterInEachList {

    public static void main(String args[]) throws IOException, InvalidFormatException {
        String inputFN = "C:\\Users\\njuli\\Desktop\\实验结果\\大数据检测结果\\final Scope.xlsx";
        String outputFN = "C:\\Users\\njuli\\Desktop\\实验结果\\大数据检测结果\\final Scope.xlsx";

        File inputF = new File(inputFN);
        Workbook workbook = WorkbookFactory.create(new FileInputStream(inputF));


        Sheet unionSetSheet = workbook.getSheet("UnionSet");
        Sheet amSheet = workbook.getSheet("AmCheck (2)");
        Sheet caSheet = workbook.getSheet("CACheck (2)");
        Sheet cuSheet = workbook.getSheet("CUSTODES (2)");
        Sheet waSheet = workbook.getSheet("WARDER (2)");

        existOrNot(unionSetSheet, unionSetSheet, 20);
        /*
        existOrNot(unionSetSheet, amSheet, 4);
        existOrNot(unionSetSheet, caSheet, 5);
        existOrNot(unionSetSheet, cuSheet, 6);
        existOrNot(unionSetSheet, waSheet, 7);

        FileOutputStream resultStream = new FileOutputStream(new File(outputFN));
        workbook.write(resultStream);
        resultStream.close();
        */
    }

    private static void existOrNot(Sheet unionSetSheet, Sheet waSheet, int columnIndex) {
        for (Row row: waSheet) {
            if (row.getRowNum() == 0) continue;
            Cell nonLastCell =  row.getCell(1);
            if (nonLastCell == null) continue;
            String SSName = row.getCell(1).getStringCellValue();
            String WSName = row.getCell(2).getStringCellValue();

            for (Row nonTCRow: unionSetSheet) {
                String nonTCSSName = nonTCRow.getCell(1).getStringCellValue();
                String nonTCWSName = nonTCRow.getCell(2).getStringCellValue();
                if (nonTCSSName.equals(SSName) && nonTCWSName.equals(WSName)) {
                    if (row.getRowNum() != nonTCRow.getRowNum()) {
                        Cell nonTCCell = nonTCRow.createCell(columnIndex);
                        nonTCCell.setCellValue(1);
                        System.err.println("duplicate worksheet: " + nonTCSSName + "/" + nonTCWSName);
                    }
                }
            }

        }
    }
}
