package programEntry;

import experiment.StatisticsForAll;
import experiment.StatisticsForSheet;
import org.apache.poi.EmptyFileException;
import org.apache.poi.hssf.OldExcelFormatException;
import org.apache.poi.hssf.record.RecordInputStream;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static programEntry.GP.*;
import static programEntry.GP.fileSeparator;
import static programEntry.GP.prefixOutDir;
import static programEntry.MainClass.ssNameList;
import static programEntry.TestDataSet.upperLimit;
import static programEntry.TestWorksheet.testWorksheet;

public class TestSpreadsheet {

    public static void testSpreadsheet(File file, StatisticsForAll staAll, BufferedWriter logBuffer,
                                       AtomicInteger index, boolean test, String category)
            throws Exception, OutOfMemoryError {

        int identicalIndex = index.incrementAndGet();
        System.out.println("index = " + identicalIndex + " ######## begin: " +
                "/" + file.getName() + "'########");
        //logBuffer.write("index = " + identicalIndex + " ######## begin: " +
        //"/" + file.getName() + "'########");
        //logBuffer.newLine();

        if (identicalIndex > upperLimit) return;

        String fileName = null;
        Workbook workbook;
        try {
            fileName = file.getName();
            workbook = WorkbookFactory.create(new FileInputStream(file));
        }
        catch (OldExcelFormatException oefe) {
            System.out.println("Old Excel Format E-xception happened.");
            workbook = null;
        }
        catch (InvalidFormatException ife) {
            System.out.println("Invalid Format E-xception happened.");
            workbook = null;
        }
        catch (EmptyFileException EFE) {
            System.out.println("MainClass @200 line: Empty File E-xception.");
            workbook = null;
        }
        catch (RecordInputStream.LeftoverDataException LDE) {
            System.out.println("MainClass @200 line: Left Over Data E-xception.");
            workbook = null;
        }

        if (workbook == null) {
            System.out.println("Spreadsheet index = "+ identicalIndex +" ######## End in: '" + fileName + "'########");
            System.out.println("FinishedSS = " + finishedSS.incrementAndGet());
            System.out.println();
//            logBuffer.write("Spreadsheet index = "+ identicalIndex +" ######## End in: " + fileName + "'########");
            ssNameList.add(fileName);
            addVirtualSS(category, fileName, 1);
            return;
        }

        AtomicBoolean flagAdd = new AtomicBoolean(false);
        for (int j = 0; j < workbook.getNumberOfSheets(); j++) {
            //TODO: test the specific worksheet
//            if (!workbook.getSheetAt(j).getSheetName().contains("Table II.4")) continue;

            String finalFileName = fileName;
            Sheet curSheet = workbook.getSheetAt(j);

            exeService.execute(() -> {
                try {
                    StatisticsForSheet staSheet = testWorksheet(finalFileName, curSheet, logBuffer, test, category);
                    if (staAll.add(staSheet, logBuffer))
                        flagAdd.set(true);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }

            });

        }

        if (!flagAdd.get()) {
            addVirtualSS(category, fileName, 2);
        }

        String eachDirStr = outDirPath + fileSeparator + "Marked subjects " + testDate;

        File eachDir = new File(eachDirStr);
        if (!eachDir.exists()) {
            eachDir.mkdir();
        }

        String categoryDirStr = eachDirStr + fileSeparator + category;
        File categoryDir = new File(categoryDirStr);
        if (!categoryDir.exists()) {
            categoryDir.mkdir();
        }

        String suffix = null;
        if (workbook .getSpreadsheetVersion().name().equals("EXCEL97"))
            suffix = "xls";
        else if (workbook .getSpreadsheetVersion().name().equals("EXCEL2007"))
            suffix = "xlsx";
        String outFileStr = categoryDirStr + fileSeparator + fileName.substring(0, fileName.lastIndexOf('.'))
                + "_" + GP.addSuffix() + "." + suffix;

        FileOutputStream outFile = new FileOutputStream(outFileStr);
        workbook.write(outFile);
        outFile.close();

        System.out.println("Spreadsheet index = "+ identicalIndex +" ######## End in: '" + fileName + "'########");
        System.out.println("FinishedSS = " + finishedSS.incrementAndGet());
        System.out.println();
        //logBuffer.write("Spreadsheet index = "+ identicalIndex +" ######## End in: '" + fileName + "'########");
        //logBuffer.newLine();
        ssNameList.add(fileName);

        //在每个SS执行完之后立刻输出当前所有执行完的SS的综合信息
        staAll.log(prefixOutDir, true, null);
    }


    private static void addVirtualSS(String category, String fileName, int situation) throws IOException {
        StatisticsForSheet staSheet = new StatisticsForSheet(null, category, situation);
        staSheet.setSpreadsheet(fileName);
        staAll.add(staSheet, logBuffer);
        staAll.log(prefixOutDir, true, null);
    }
}
