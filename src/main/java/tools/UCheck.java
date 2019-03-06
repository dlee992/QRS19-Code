package tools;

import kernel.GP;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import statistics.Worksheet_result;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class UCheck {

    static String toolName = "UCheck";

    public static void main(String args[]) throws IOException, InvalidFormatException {
        computeDetectionResult(toolName);
    }

    public static void computeDetectionResult(String toolName) throws IOException, InvalidFormatException {
        int TP = 0, FP = 0, FN = 0;

        File tool_dir = new File(GP.outDirPath, toolName);
        File tool_result = new File(tool_dir.getPath(), toolName + " results.xls");
        File gt_result = new File(GP.outDirPath, "GroundTruth results.xls");

        Workbook gt_workbook = WorkbookFactory.create(new FileInputStream(gt_result));
        Sheet gt_sheet = gt_workbook.getSheetAt(0);
        Workbook tool_workbook = WorkbookFactory.create(new FileInputStream(tool_result));
        Sheet tool_sheet = tool_workbook.getSheetAt(0);

        for (int i = 1; i<= gt_sheet.getLastRowNum(); i++) {
            Row gt_row = gt_sheet.getRow(i);
            Worksheet_result gt_sheet_result = new Worksheet_result();
            gt_sheet_result.handleOneRow(gt_row);

            boolean matched_worksheet = false;
            for (int j=1; j<= tool_sheet.getLastRowNum(); j++) {
                Row tool_row = tool_sheet.getRow(j);
                Worksheet_result tool_sheet_result = new Worksheet_result();
                tool_sheet_result.handleOneRow(tool_row);

                if (gt_sheet_result.spreadsheet.equals(tool_sheet_result.spreadsheet) &&
                        gt_sheet_result.sheet.equals(tool_sheet_result.sheet)) {
                    matched_worksheet = true;
                    for (String gt_smell: gt_sheet_result.smellList) {
                        if (tool_sheet_result.smellList.contains(gt_smell)) TP++;
                        else FN++;
                    }
                    break;
                }
            }

            if (!(matched_worksheet)) FN+=gt_sheet_result.smellList.size();
        }

        for (int j=1; j<= tool_sheet.getLastRowNum(); j++) {
            Row tool_row = tool_sheet.getRow(j);
            Worksheet_result tool_sheet_result = new Worksheet_result();
            tool_sheet_result.handleOneRow(tool_row);

            boolean matched_worksheet = false;
            for (int i = 1; i<= gt_sheet.getLastRowNum(); i++) {
                Row gt_row = gt_sheet.getRow(i);
                Worksheet_result gt_sheet_result = new Worksheet_result();
                gt_sheet_result.handleOneRow(gt_row);

                if (gt_sheet_result.spreadsheet.equals(tool_sheet_result.spreadsheet) &&
                        gt_sheet_result.sheet.equals(tool_sheet_result.sheet)) {
                    matched_worksheet = true;
                    for (String tool_smell: tool_sheet_result.smellList) {
                        if (!(gt_sheet_result.smellList.contains(tool_smell))) FP++;
                    }
                    break;
                }
            }

            if (!(matched_worksheet)) FP+=tool_sheet_result.smellList.size();
        }

        System.out.println(TP + ", " + FP + ", " + FN);
    }

}
