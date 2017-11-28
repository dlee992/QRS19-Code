package ThirdParty.CACheck.cellarray.extract;

import java.util.List;

import org.apache.poi.ss.usermodel.Cell;

import ThirdParty.CACheck.AMSheet;
import ThirdParty.CACheck.CellArray;
import ThirdParty.CACheck.R1C1Cell;
import ThirdParty.CACheck.amcheck.AnalysisPattern;
import ThirdParty.CACheck.cellarray.synthesis.SynthesisUtils;
import ThirdParty.CACheck.formula.ParseFormula;
import ThirdParty.CACheck.util.CellUtils;
import ThirdParty.CACheck.util.Log;

public class CorrectCellArrayExtractor extends CellArrayExtractor {

	public CorrectCellArrayExtractor(AMSheet sheet, AnalysisPattern analysisPattern) {
		super(sheet, analysisPattern);
	}

	protected int splitCA(CellArray ca, List<CellArray> cas) {
		for (int i = 0; i < ca.size(); i++) {
			Cell cell = ca.getCell(sheet, i);
			if (cell == null || !CellUtils.isFormula(cell)) {
				return ca.start + i;
			}
		}

		for (int start = ca.start; start < ca.end; start++) {

			try {
				// make an aggressive check.
				for (int end = start + 1; end <= ca.end && end - start <= 5; end++) {
					CellArray newCA = new CellArray(ca.rowOrColumn, ca.isRowCA,
							start, end);
					if (!isCellArray(newCA)) {
						return end;
					}
				}

				// total search.
				for (int end = ca.end; end > start; end--) {
					CellArray newCA = new CellArray(ca.rowOrColumn, ca.isRowCA,
							start, end);
					if (isCellArray(newCA)) {
						cas.add(newCA);
						start = end;
						break;
					}
				}
			} catch (Exception e) {
				Log.logNewLine(e, Log.writer);
			}
		}

		return -1;
	}

	private boolean isCellArray(CellArray ca) {

		if (isCellArray(ca, false)) {
			return true;
		} else {
			return isCellArray(ca, true);
		}
	}

	private boolean isCellArray(CellArray ca, boolean checkConstants) {

		if (checkConstants) {
			// Find constants, and change them.
			List<Dependence> deps = DependenceConstantExtractor
					.constructDependence(sheet, ca);
			List<R1C1Cell> posConstants = DependenceConstantExtractor
					.extractPossibleConstants(deps, 1.0f);
			ca.constants = posConstants;
		}

		List<Object> prePattern = null;
		for (int i = 0; i < ca.size(); i++) {
			Cell cell = ca.getCell(sheet, i);
			List<Object> pattern = ParseFormula.parse(ca, cell);
			if (pattern == null) {
				return false;
			}

			if (prePattern == null) {
				prePattern = pattern;
			} else {
				if (!SynthesisUtils.semanticEqual(prePattern, pattern)) {
					return false;
				}
			}
		}

		return true;
	}
}
