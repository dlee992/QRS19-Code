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
import static programEntry.MainClass.ssNameList;

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
        catch (RuntimeException ignored) {
            System.out.println("MainClass @200 line: Index Out Of Bound E-xception.");
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

        String eachDirStr = outDirPath + fileSeparator + testDate;

        File eachDir = new File(eachDirStr);
        if (!eachDir.exists()) {
            eachDir.mkdir();
        }

        String categoryDirStr = eachDirStr + fileSeparator + category;
        File categoryDir = new File(categoryDirStr);
        if (!categoryDir.exists()) {
            categoryDir.mkdir();
        }

        AtomicBoolean flagAdd = new AtomicBoolean(false);

        printFlag.put(fileName, new AtomicInteger(workbook.getNumberOfSheets()));

        System.out.println("sheet NO = " + workbook.getNumberOfSheets());
        for (int j = 0; j < workbook.getNumberOfSheets(); j++) {
            //TODO: test the specific worksheet
//            if (!workbook.getSheetAt(j).getSheetName().contains("Individual")) continue;

            Sheet curSheet = workbook.getSheetAt(j);

            TestWorksheet testWorksheetTask = new TestWorksheet(fileName, curSheet, logBuffer, test,
                    category, categoryDirStr);
            tasks.add(testWorksheetTask);
            futures.add(exeService.submit(testWorksheetTask));
        }

        //TODO:这里注释掉了 可能在最终的输出上不完整 丢失了那些没有任何信息的spreadsheet
//        if (!flagAdd.get()) {
//            addVirtualSS(category, fileName, 2);
//        }



        //并发，导致下面的代码优先于其他线程先执行结束
        System.out.println("Spreadsheet index = "+ identicalIndex +" ######## (Fake) End in: '" + fileName + "'########");
        System.out.println("FinishedSS = " + finishedSS.incrementAndGet());
        System.out.println();
        //logBuffer.write("Spreadsheet index = "+ identicalIndex +" ######## End in: '" + fileName + "'########");
        //logBuffer.newLine();
        ssNameList.add(fileName);

        //在每个SS执行完之后立刻输出当前所有执行完的SS的综合信息
//        staAll.log(prefixOutDir, true, null);
    }


    private static void addVirtualSS(String category, String fileName, int situation) throws IOException {
        StatisticsForSheet staSheet = new StatisticsForSheet(null, category, situation);
        staSheet.setSpreadsheet(fileName);
        staAll.add(staSheet, logBuffer);
//        staAll.log(prefixOutDir, true, null);
    }
}

