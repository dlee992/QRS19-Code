package testGetMethod;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by yolanda on 17-3-28.
 *
 * It is used to compare two output results, especially the difference, i.e., new emergence and discard, in TP and FP,
 * between original result and one optimization result.
 */
public class ComparisonInResults {
    private static String fileSeparator = System.getProperty("file.separator");

    public static void mainx(String[] args) throws IOException {
        String file1 = "filterString(base).xlsx";
        String[] fileList = {"filterString+O3+0.1.xlsx"};//, "filterString+O3+0.1.xlsx"};
//                {"filterString+O2.1+0.01+OR.xlsx", "filterString+O2.1+0.001+OR.xlsx", "filterString+O2.1+0.0001+OR.xlsx",
//                "filterString+O2.1+0.0001+AND.xlsx"};
        for (String file2: fileList
             )  {
            compare(file1, file2);
        }
    }

    public static void compare(String file1, String file2) throws IOException {

        String parentFilePath = System.getProperty("user.dir") + fileSeparator + "Outputs";
        String filePrefix = parentFilePath + fileSeparator;

        String baseFile = filePrefix + file1;
        String optFile = filePrefix + file2;

        Workbook baseWB = new XSSFWorkbook(new FileInputStream(new File(baseFile)));
        Workbook optWB = new XSSFWorkbook(new FileInputStream(new File(optFile)));

        Sheet baseSheet = baseWB.getSheetAt(0);
        Sheet optSheet = optWB.getSheetAt(0);

        for (Row baseRow : baseSheet) {
            System.out.println("Row------" + baseRow.getRowNum() + "------");
            if (baseRow.getRowNum() == 0) {
                //between baseline (not ground truth) and Optimization results.
                Row zeroRow = optSheet.getRow(0);
                zeroRow.createCell(12).setCellValue("Identical TP");
                zeroRow.createCell(13).setCellValue("New TP");
                zeroRow.createCell(14).setCellValue("Lost TP");

                zeroRow.createCell(15).setCellValue("Identical FP");
                zeroRow.createCell(16).setCellValue("New FP");
                zeroRow.createCell(17).setCellValue("Lost FP");
                continue;
            }
//            if (baseRow.getRowNum() >= 5) continue;
            if (baseRow.getRowNum() == 278) {
                Row lastRow = optSheet.getRow(278);
                lastRow.createCell(12).setCellValue(sum(optSheet, 12));
                lastRow.createCell(13).setCellValue(sum(optSheet, 13));
                lastRow.createCell(14).setCellValue(sum(optSheet, 14));
                lastRow.createCell(15).setCellValue(sum(optSheet, 15));
                lastRow.createCell(16).setCellValue(sum(optSheet, 16));
                lastRow.createCell(17).setCellValue(sum(optSheet, 17));
                continue;
            }

            Row optRow = findRow(optSheet, baseRow);

            Cell gtCell = baseRow.getCell(10), baseCell = baseRow.getCell(11), optCell = optRow.getCell(11);

            Set<String> gtSet =
                    generateFromLiteral(gtCell == null? null : gtCell.getStringCellValue());
            Set<String> baseSet =
                    generateFromLiteral(baseCell == null? null : baseCell.getStringCellValue());
            Set<String> optSet =
                    generateFromLiteral(optCell == null? null : optCell.getStringCellValue());

            Set<String> tp1 = intersection(gtSet, baseSet), tp2 = intersection(gtSet, optSet);
            Set<String> fp1 = complement(baseSet, gtSet), fp2 = complement(optSet, gtSet);

            printSet(gtSet);printSet(baseSet);printSet(optSet);
            printSet(tp1);printSet(tp2);printSet(fp1);printSet(fp2);


            optRow.createCell(12).setCellValue(intersection(tp1, tp2).size());
            optRow.createCell(13).setCellValue(complement(tp2, tp1).size());
            optRow.createCell(14).setCellValue(complement(tp1, tp2).size());
            optRow.createCell(15).setCellValue(intersection(fp1, fp2).size());
            optRow.createCell(16).setCellValue(complement(fp2, fp1).size());
            optRow.createCell(17).setCellValue(complement(fp1, fp2).size());

            optRow.createCell(12+6).setCellValue(setToString(intersection(tp1, tp2)));
            optRow.createCell(13+6).setCellValue(setToString(complement(tp2, tp1)));
            optRow.createCell(14+6).setCellValue(setToString(complement(tp1, tp2)));
            optRow.createCell(15+6).setCellValue(setToString(intersection(fp1, fp2)));
            optRow.createCell(16+6).setCellValue(setToString(complement(fp2, fp1)));
            optRow.createCell(17+6).setCellValue(setToString(complement(fp1, fp2)));
        }



        FileOutputStream myOutFile = new FileOutputStream(filePrefix + file2);
        optWB.write(myOutFile);
    }

    private static String setToString(Set<String> set) {
        StringBuffer strBuf = new StringBuffer();
        for (String cellAdr: set
             ) {
            strBuf.append(cellAdr + ", ");
        }
        return strBuf.toString();
    }

    static int nullNumber = 0;
    private static Set<String> generateFromLiteral(String literal) {

        Set<String> set = new HashSet<String>();
        if (literal == null || literal.length() == 0) {
            System.out.println(++nullNumber);
            return set;
        }
//        System.out.println("["+literal+"]"+literal.length());
        String[] cellAddresses = literal.replaceAll("[^0-9A-Z,]", "").split(",");

        for (String cellAddress : cellAddresses
                ) {
            set.add(cellAddress);
        }

        return set;
    }



    private static Set<String> intersection(Set<String> set1, Set<String> set2) {
        Set<String> return_set = new HashSet<String>();
        for (String cellAdr: set1
             ) {
            if (set2.contains(cellAdr))
                return_set.add(cellAdr);
        }
        return return_set;
    }



    private static Set<String> complement(Set<String> set1, Set<String> set2) {
        Set<String> return_set = new HashSet<String>();
        for (String cellAdr: set1
                ) {
            if (set2.contains(cellAdr) == false)
                return_set.add(cellAdr);
        }
        return return_set;
    }


    static int line = 0;
    private static void printSet(Set<String> set) {
        System.out.print(++line+":");

        for (String i: set
             ) {
            System.out.print(i + ",");
        }
        System.out.println();
    }



    static Row findRow(Sheet sheet2, Row row1) {
        System.out.println(row1.getCell(1).getStringCellValue());
        System.out.println(row1.getCell(2).getStringCellValue());
        for (Row row2: sheet2
             ) {

            if (row1.getCell(1).getStringCellValue().equals(row2.getCell(1).getStringCellValue())
                    && row1.getCell(2).getStringCellValue().equals(row2.getCell(2).getStringCellValue()) ) {
                return row2;
            }
        }

        // just for passing the compiling checker.
        return null;
    }

    static int sum(Sheet sheet, int columnNumber) {
        int return_sum = 0;
        for (int i = 1; i < 278; i++) {
            return_sum += (int)sheet.getRow(i).getCell(columnNumber).getNumericCellValue();
        }
        return return_sum;
    }
}
