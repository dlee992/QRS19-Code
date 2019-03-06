package statistics;

import kernel.GP;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GroundTruthDump {

    public static void main(String args[]) throws IOException, InvalidFormatException {
        GroundTruthDump gtd = new GroundTruthDump();
        gtd.mainBody();
    }

    public void mainBody() throws IOException, InvalidFormatException {
        File inputDir = new File(GP.parent_dir, "Inputs");
        File GTDir = new File(inputDir.getPath(), "Groundtruth");

        Workbook workbook = new HSSFWorkbook();
        Sheet sheet = workbook.createSheet("result of " + "GroundTruth");
        int rowIndex = 0, smellCount = 0;
        Row row = sheet.createRow(rowIndex++);
        row.createCell(0).setCellValue("Category");
        row.createCell(1).setCellValue("Spreadsheet");
        row.createCell(2).setCellValue("Worksheet");
        row.createCell(3).setCellValue("# Smells");
        row.createCell(4).setCellValue("List of Smells");

        for (File file : GTDir.listFiles()) {

            Workbook currentWorkbook = WorkbookFactory.create(new FileInputStream(file));

            Sheet currentSheet = currentWorkbook.getSheetAt(0);

            List<String> smellList = new ArrayList<>();
            for (Row r : currentSheet)
                for (Cell cell : r)
                    if (cell.getCellComment() != null) {
                        smellList.add(new CellReference(cell).formatAsString());
                        smellCount++;
                    }

            // dump current sheet information
            row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue("Unknown");
            row.createCell(1).setCellValue(file.getName().substring(0, file.getName().lastIndexOf('_'))+".xls");
            row.createCell(2).setCellValue(currentSheet.getSheetName());
            row.createCell(3).setCellValue(smellList.size());
            row.createCell(4).setCellValue(smellList.toString());
        }

        System.out.println("SmellCount = " + smellCount);
        File desFile = new File(inputDir.getPath(), "GroundTruth results.xls");
        if (!desFile.exists()) desFile.createNewFile();
        FileOutputStream output = new FileOutputStream(desFile);
        workbook.write(output);
        output.close();
    }
}
