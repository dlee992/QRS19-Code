package featureExtraction.weakFeatureExtraction;

import ThirdParty.CACheck.AMSheet;
import ThirdParty.CACheck.amcheck.AnalysisPattern;
import ThirdParty.CACheck.amcheck.ExcelPreProcess;
import ThirdParty.CACheck.cellarray.extract.CAResult;
import ThirdParty.CACheck.snippet.*;

import ThirdParty.CACheck.util.Log;
import ThirdParty.CACheck.util.SpreadsheetMark;
import ThirdParty.CACheck.util.Utils;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static ThirdParty.CACheck.amcheck.ExcelAnalysis.processSnippet;

/**
 * Created by lida on 2017/5/4.
 */
public class Wrapper {
    private Sheet st = null;
    private AnalysisPattern analysisPattern = new AnalysisPattern();

    public void setSheet(Sheet sheet) {
        this.st = sheet;
    }

    public Wrapper(int type) {
        analysisPattern.setType(type);
    }

    public Wrapper(Sheet sheet, int type) {
        this.st = sheet;
        analysisPattern.setType(type);
    }

    public List<CellArray> processSheet() {
        try {
            //TODO: log file should be removed.
            String parent_dir = System.getProperty("user.dir");
            String logFile = parent_dir + System.getProperty("file.separator") + "log.txt";
            Log.writer = new BufferedWriter(new FileWriter(logFile, true));

//            Log.logNewLine("----Sheet '" + st.getSheetName() + "'----", Log.writer);

            if (ExcelPreProcess.countFormulas(st) == 0) {
                // no formula
                return new ArrayList<>();
            }

            List<CAResult> allCARs = new ArrayList<CAResult>();
            //fixme: from xlsFile.getName() to sheet.getSheetName().
            AMSheet sheet = Utils.extractSheet(st, st.getSheetName());

            ExtractSnippet extractSnippet = new ExtractSnippet(sheet);
            List<ThirdParty.CACheck.snippet.Snippet> snippets = extractSnippet.extractSnippet();
            for (ThirdParty.CACheck.snippet.Snippet snippet : snippets) {
                List<CAResult> tmp = processSnippet(st.getSheetName(), sheet,
                        snippet, snippets, Log.writer, analysisPattern);
                allCARs.addAll(tmp);
            }

            SpreadsheetMark.markDetectResult(sheet, allCARs);

            //todo: from allCARs to allCAs.
            List<CellArray> allCAs = new ArrayList<>();
            for (CAResult car: allCARs
                    ) {
                ThirdParty.CACheck.CellArray caca = car.cellArray;
                CellArray ca = new CellArray(caca.rowOrColumn, caca.isRowCA, caca.start, caca.end);
                allCAs.add(ca);
            }

            return allCAs;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    public static void main(String[] args) throws Exception {
        System.out.println(System.getenv("JAVA_LIBRARY_PATH"));
        String logFile = "/Users/lida/Desktop/log.txt";
        Log.writer = new BufferedWriter(new FileWriter(logFile, true));

        File xlsFile = new File("/Users/lida/Desktop/test.xls");
        Workbook workbook = WorkbookFactory.create(new FileInputStream(xlsFile));
        Sheet st = workbook.getSheetAt(0);

        Wrapper wrapper = new Wrapper(st, 6);
        wrapper.processSheet();

        File outFilepath = new File("/Users/lida/Desktop/testOutput.xls");
        FileOutputStream outFile = new FileOutputStream(outFilepath);
        workbook.write(outFile);
        outFile.close();
        workbook.close();
    }
}
