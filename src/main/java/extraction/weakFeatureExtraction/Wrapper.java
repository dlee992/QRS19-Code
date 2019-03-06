package extraction.weakFeatureExtraction;

import org.apache.poi.ss.usermodel.Sheet;
import thirdparty.CACheck.AMSheet;
import thirdparty.CACheck.amcheck.AnalysisPattern;
import thirdparty.CACheck.amcheck.ExcelPreProcess;
import thirdparty.CACheck.cellarray.extract.CAResult;
import thirdparty.CACheck.snippet.ExtractSnippet;
import thirdparty.CACheck.util.Log;
import thirdparty.CACheck.util.SpreadsheetMark;
import thirdparty.CACheck.util.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static thirdparty.CACheck.amcheck.ExcelAnalysis.processSnippet;

/**
 * Created by lida on 2017/5/4.
 */
public class Wrapper {
    public Sheet sheet;
    public AnalysisPattern analysisPattern = new AnalysisPattern();

    public Wrapper(Sheet sheet, int type) {
        this.sheet = sheet;
        analysisPattern.setType(type);
    }

    public List<CAResult> processSheet(String toolName) throws IOException {

        if (ExcelPreProcess.countFormulas(sheet) == 0) return null;

        List<CAResult> allCARs = new ArrayList<>();
        AMSheet amSheet = Utils.extractSheet(this.sheet, this.sheet.getSheetName());
        ExtractSnippet extractSnippet = new ExtractSnippet(amSheet);
        List<thirdparty.CACheck.snippet.Snippet> snippets = extractSnippet.extractSnippet();

        for (thirdparty.CACheck.snippet.Snippet snippet : snippets) {
            List<CAResult> tmp = processSnippet(this.sheet.getSheetName(), amSheet,
                    snippet, snippets, Log.writer, analysisPattern);
            allCARs.addAll(tmp);
        }

        SpreadsheetMark.markDetectResult(amSheet, allCARs);

        return allCARs;

        /*
        // from allCARs to allCAs.
        List<CellArray> allCAs = new ArrayList<>();
        for (CAResult car: allCARs
                ) {
            thirdparty.CACheck.CellArray caca = car.cellArray;
            CellArray ca = new CellArray(caca.rowOrColumn, caca.isRowCA, caca.start, caca.end);
            allCAs.add(ca);
        }

        return allCAs;
        */
    }

}
