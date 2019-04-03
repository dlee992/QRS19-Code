package DataCollection;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CaseStudyIntersection {


    public static void main(String args[]) throws IOException, InvalidFormatException {
        // read 300 worksheet info
        // read ground-truth worksheet's defects marked by RED background color
        // read each technique's defect list
        // count in ground-truth with techniques' defect lists
        String inputFN = "A:\\CC1s-concurrent\\Inputs\\FilterList.xlsx";
        String outputFN = "A:\\CC1s-concurrent\\Inputs\\FilterList-count.xlsx";
        String GTDirPath = "A:\\CC1s-concurrent\\Inputs\\CaseStudyGroundTruth";
        File inputF = new File(inputFN);
        Workbook workbook = WorkbookFactory.create(new FileInputStream(inputF));
        File GtDir = new File(GTDirPath);

        Sheet sheet = workbook.getSheet("300");
        Sheet AMSheet = workbook.getSheet("AM");
        Sheet CASheet = workbook.getSheet("CA");
        Sheet CUSheet = workbook.getSheet("CU");
        Sheet WASheet = workbook.getSheet("WA");

        int TPIntersection[] = new int[20];
        int TPData[] = new int[10];

        for (int i=1; i<=296; i++) {
            Row row = sheet.getRow(i);
            String category = Integer.toString((int)row.getCell(0).getNumericCellValue());
            if (category.equals("1353") || category.equals("1414")) continue;

            String ss = row.getCell(1).getStringCellValue();
            String ws = row.getCell(2).getStringCellValue();

            boolean listMatch = false;
            for (File index: GtDir.listFiles()) {
                if (index.getName().equals(category)) {
                    listMatch = true;

                    List<String> AMList = listOfIndex(category, ss, ws, AMSheet, 1);
                    List<String> CAList = listOfIndex(category, ss, ws, CASheet, 2);
                    List<String> CUList = listOfIndex(category, ss, ws, CUSheet,3);
                    List<String> WAList = listOfIndex(category, ss, ws, WASheet,4);

                    //
                    Workbook GTWorkbook = WorkbookFactory.create(new FileInputStream(index.listFiles()[0]));
                    Sheet GTSheet = GTWorkbook.getSheet(ws);
                    List<String> GTList = new ArrayList<>();
                    List<String> GTRedList = new ArrayList<>();
                    List<String> GTRedDataList = new ArrayList<>();
                    List<Boolean> GTBool = new ArrayList<>();
                    for (Row GTRow : GTSheet) {
                        for (Cell GTCell : GTRow) {
                            if (GTCell.getCellStyle().getFillForegroundColor() != IndexedColors.AUTOMATIC.getIndex() &&
                                GTCell.getCellStyle().getFillForegroundColor() != IndexedColors.WHITE.getIndex()) {
                                GTList.add(new CellReference(GTCell).formatAsString());
                                if (GTCell.getCellStyle().getFillForegroundColor() == 10) //red
                                {
                                    GTRedList.add(new CellReference(GTCell).formatAsString());
                                    if (GTCell.getCellTypeEnum() == CellType.NUMERIC)
                                        GTRedDataList.add(new CellReference(GTCell).formatAsString());
                                }
                                GTBool.add(false);
                            }
                        }
                    }

                    List<String> AMTPList = computeTPCount(AMSheet, category, ss, ws, AMList, GTRedList);
                    List<String> CATPList = computeTPCount(CASheet, category, ss, ws, CAList, GTRedList);
                    List<String> CUTPList = computeTPCount(CUSheet, category, ss, ws, CUList, GTRedList);
                    List<String> WATPList = computeTPCount(WASheet, category, ss, ws, WAList, GTRedList);

                    for (String cellLocation: GTRedDataList
                         ) {
                        if (AMTPList.contains(cellLocation)) TPData[0]++;
                        if (CATPList.contains(cellLocation)) TPData[1]++;
                        if (CUTPList.contains(cellLocation)) TPData[2]++;
                        if (WATPList.contains(cellLocation)) TPData[3]++;
                    }

                    //0
                    for (String cellLocation: AMTPList
                         ) {
                        if (CATPList.contains(cellLocation)) {
                            TPIntersection[0]++;
                        }
                        if (CUTPList.contains(cellLocation)) {
                            TPIntersection[1]++;
                        }
                        if (WATPList.contains(cellLocation)) {
                            TPIntersection[2]++;
                        }

                        if (CATPList.contains(cellLocation) && CUTPList.contains(cellLocation)) {
                            TPIntersection[6]++;
                        }
                        if (CATPList.contains(cellLocation) && WATPList.contains(cellLocation)) {
                            TPIntersection[7]++;
                        }
                        if (CUTPList.contains(cellLocation) && WATPList.contains(cellLocation)) {
                            TPIntersection[8]++;
                        }
                        if (CATPList.contains(cellLocation) && CUTPList.contains(cellLocation) && WATPList.contains(cellLocation)) {
                            TPIntersection[10]++;
                        }
                    }


                    //3
                    for (String cellLocation: CATPList
                    ) {
                        if (CUTPList.contains(cellLocation)) {
                            TPIntersection[3]++;
                        }
                        if (WATPList.contains(cellLocation)) {
                            TPIntersection[4]++;
                        }
                        if (CUTPList.contains(cellLocation) && WATPList.contains(cellLocation)) {
                            TPIntersection[9]++;
                        }
                    }

                    //5
                    for (String cellLocation: CUTPList
                    ) {
                        if (WATPList.contains(cellLocation)) {
                            TPIntersection[5]++;
                        }
                    }


                    //System.out.println("GT="+GTList);

                    /*
                    validate(GTList, GTBool, AMList, 1, category, ss, ws);
                    validate(GTList, GTBool, CAList, 2, category, ss, ws);
                    validate(GTList, GTBool, CUList, 3, category, ss, ws);
                    validate(GTList, GTBool, WAList, 4, category, ss, ws);
                    for (int j = 0; j < GTList.size(); j++)
                        if (!GTBool.get(j)) {
                            System.err.println("%^&*" + category + "/" + ss + "/" + ws + ": cell [" + GTList.get(j) + "] do not lie in any tool result.");
                        }
                    */
                    break;
                }
            }

            if (!listMatch) {
                System.err.println(category+"/"+ss+"/"+ws+" do not exist in ground-truth.");
            }
        }

        for (int i = 0; i < 4; i++) {
            System.out.println(TPData[i]);
        }
        System.err.println("--separator--");
        for (int i = 0; i <= 10; i++) {
            System.out.println(TPIntersection[i]);
        }
        /*
        File outputF = new File(outputFN);
        if (!outputF.exists()) outputF.createNewFile();
        FileOutputStream outputFS = new FileOutputStream(outputF);
        workbook.write(outputFS);
        outputFS.close();
        */
    }

    private static List<String> computeTPCount(Sheet detectedSheet, String category, String ss, String ws,
                                       List<String> detectedList, List<String> GTRedList) {
        List<String> returned = new ArrayList<>();
        int TPCount = 0;
        for (String cellLocation: GTRedList) {
            if (detectedList.contains(cellLocation)) {
                returned.add(cellLocation);
                TPCount++;
            }
        }
        return returned;

/*
        for (Row detectedRow: detectedSheet) {
            String detectedcategory = null;
            if (detectedRow.getCell(0) == null) continue;
            if (detectedRow.getCell(0).getCellType() == 0)
                detectedcategory = Integer.toString((int) detectedRow.getCell(0).getNumericCellValue());
            else
                detectedcategory = detectedRow.getCell(0).getStringCellValue();
            String detectedss = detectedRow.getCell(1).getStringCellValue();
            String detectedws = detectedRow.getCell(2).getStringCellValue();

            if (detectedcategory.equals(category) && detectedss.equals(ss) && detectedws.equals(ws)) {
                detectedRow.createCell(12).setCellValue(TPCount);
            }
        }
        */
    }


    private static void validate(List<String> gtList, List<Boolean> GTBool, List<String> detectedList, int index,
                                 String category, String ss, String ws) {

        if (detectedList == null || (detectedList != null && gtList == null)) {
            System.err.println(category+"/"+ss+"/"+ws+ ": " +index + " DetectedList does match with gtList");
            return;
        }
        for (String cellLocation: detectedList) {
            if (!gtList.contains(cellLocation)) {
                System.err.println(category+"/"+ss+"/"+ws+ ": " +index + " DetectedList cell " + cellLocation
                        + " is not included in gtList");
            }
            else {
                GTBool.set(gtList.indexOf(cellLocation), true);
            }
        }
    }

    private static List<String> listOfIndex(String category, String ss, String ws, Sheet detectedSheet, int index) {
        List<String> smellReturn = new ArrayList<>();

        for (Row detectedRow: detectedSheet) {
            String detectedcategory = null;
            if (detectedRow.getCell(0) == null) continue;
            if(detectedRow.getCell(0).getCellType() == 0)
                detectedcategory = Integer.toString((int)detectedRow.getCell(0).getNumericCellValue());
            else
                detectedcategory = detectedRow.getCell(0).getStringCellValue();
            String detectedss = detectedRow.getCell(1).getStringCellValue();
            String detectedws = detectedRow.getCell(2).getStringCellValue();

            if (detectedcategory.equals(category) && detectedss.equals(ss) && detectedws.equals(ws)) {
                String smellList = "";
                int columnIndex = 4;
                while (smellList.equals("") || smellList.charAt(smellList.length()-1) != ']') {
                    smellList += detectedRow.getCell(columnIndex).getStringCellValue();
                    columnIndex++;
                }

                //
                int stringP=0, stringQ = 0;
                while (stringP < smellList.length()) {
                    while(stringP < smellList.length() && !(smellList.charAt(stringP)>='A' && smellList.charAt(stringP)<='Z'))
                        stringP++;
                    stringQ=stringP;
                    while(stringQ < smellList.length() && ((smellList.charAt(stringQ)>='A' && smellList.charAt(stringQ)<='Z') ||
                            (smellList.charAt(stringQ)>='0' && smellList.charAt(stringQ)<='9')))
                        stringQ++;
                    if (stringP < smellList.length())
                        smellReturn.add(smellList.substring(stringP, stringQ));
                    stringP = stringQ;
                }
            }

        }

        //System.out.println("detected "+ index + ": "+smellReturn);
        return smellReturn;
    }
}
