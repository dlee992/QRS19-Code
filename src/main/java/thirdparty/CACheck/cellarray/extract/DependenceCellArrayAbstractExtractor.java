package thirdparty.CACheck.cellarray.extract;

import java.util.List;

import org.apache.poi.ss.usermodel.Cell;

import thirdparty.CACheck.AMSheet;
import thirdparty.CACheck.CellArray;
import thirdparty.CACheck.R1C1Cell;
import thirdparty.CACheck.amcheck.AnalysisPattern;
import thirdparty.CACheck.cellarray.synthesis.SynthesisUtils;
import thirdparty.CACheck.formula.ParseFormula;
import thirdparty.CACheck.util.CellUtils;
import thirdparty.CACheck.util.Log;

public abstract class DependenceCellArrayAbstractExtractor extends
		CellArrayExtractor {

	protected DependenceCellArrayAbstractExtractor(AMSheet sheet, AnalysisPattern analysisPattern) {
		super(sheet, analysisPattern);
	}

	// precondition: the cell array has formulas
	public int splitCA(CellArray ca, List<CellArray> cas) {

		for (int start = ca.start; start < ca.end; start++) {
			for (int end = ca.end; end > start; end--) {
				CellArray newCA = new CellArray(ca.rowOrColumn, ca.isRowCA,
						start, end);

				if (!hasFormula(newCA)) {
					start = end;
					break;
				}

				try {
					// catch some unexcepted exceptions
					List<Dependence> deps = DependenceConstantExtractor
							.constructDependence(sheet, newCA);
					if (isCellArray(newCA, deps)) {
						List<R1C1Cell> posConstants = DependenceConstantExtractor
								.extractPossibleConstants(deps, 0.75f);
						newCA.constants = posConstants;

						cas.add(newCA);
						start = calNewStart(newCA) - 1;
						break;
					}
				} catch (Exception e) {
					Log.logNewLine(e, Log.writer);
				}
			}
		}

		return -1;
	}

	protected abstract boolean isCellArray(CellArray ca, List<Dependence> deps);

	private int calNewStart(CellArray ca) {
		/*
		 * for (int i = ca.size() - 1; i >= 0; i--) { Cell cell =
		 * ca.getCell(sheet, i); if (cell != null && cell.getCellType() ==
		 * Cell.CELL_TYPE_FORMULA) { return ca.start + i + 1; } } return
		 * ca.start + 1;
		 */
		int i = 0;
		for (; i < ca.size(); i++) {
			Cell cell = ca.getCell(sheet, i);
			if (CellUtils.isFormula(cell)) {
				break;
			}
		}

		Cell firstFCell = ca.getCell(sheet, i);
		List<Object> firstPattern = ParseFormula.parse(ca, firstFCell);
		for (; i < ca.size(); i++) {
			Cell cell = ca.getCell(sheet, i);
			if (!CellUtils.isFormula(cell)) {
				return ca.start + i;
			}

			List<Object> pattern = ParseFormula.parse(ca, cell);
			if (!SynthesisUtils.semanticEqual(firstPattern, pattern)) {
				return ca.start + i;
			}
		}
		return ca.end + 1;

	}
}
