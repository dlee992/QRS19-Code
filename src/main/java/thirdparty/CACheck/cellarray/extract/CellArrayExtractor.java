package thirdparty.CACheck.cellarray.extract;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.util.CellRangeAddress;

import thirdparty.CACheck.AMSheet;
import thirdparty.CACheck.CellArray;
import thirdparty.CACheck.R1C1Cell;
import thirdparty.CACheck.amcheck.AnalysisPattern;
import thirdparty.CACheck.cellarray.inference.ConstraintGenerator;
import thirdparty.CACheck.formula.FormulaUtils;
import thirdparty.CACheck.formula.ParseFormula;
import thirdparty.CACheck.snippet.Snippet;
import thirdparty.CACheck.util.CellUtils;
import thirdparty.CACheck.util.Log;
import thirdparty.CACheck.util.Utils;
import thirdparty.synthesis.component.AverageComponent;
import thirdparty.synthesis.component.Component;
import thirdparty.synthesis.component.MaxComponent;
import thirdparty.synthesis.component.MinComponent;
import thirdparty.synthesis.component.SumComponent;

public abstract class CellArrayExtractor {

	public static CellArrayExtractor getCellArrayExtractor(AMSheet sheet, AnalysisPattern analysisPattern) {

		int curPattern = analysisPattern.curPattern;

		if (curPattern == AnalysisPattern.Correct_CellArray) {
			return new CorrectCellArrayExtractor(sheet, analysisPattern);
		}
		if (curPattern == AnalysisPattern.Same_Row_Or_Column_Cells) {
			return new SameCellArrayExtractor(sheet, analysisPattern);
		}
		if (curPattern == AnalysisPattern.Share_Cells) {
			return new DependenceCellArraySharedExtractor(sheet, analysisPattern);
		}
		return null;
	}

	protected AMSheet sheet = null;
	public AnalysisPattern analysisPattern;

	public CellArrayExtractor(AMSheet sheet, AnalysisPattern analysisPattern) {
		this.sheet = sheet;
		this.analysisPattern = analysisPattern;
	}

	public List<CellArray> extractCellArray(Snippet snippet) {
		List<CellArray> cas = new ArrayList<CellArray>();

		// row-based cell arrays.
		for (int row = snippet.up; row <= snippet.bottom; row++) {
			CellArray ca = new CellArray(row, true, snippet.left, snippet.right);
			extract(ca, cas, row == snippet.up);
		}

		// column-based cell arrays.
		for (int col = snippet.left; col <= snippet.right; col++) {
			CellArray ca = new CellArray(col, false, snippet.up, snippet.bottom);
			extract(ca, cas, col == snippet.left);
		}

		System.out.println(sheet.xslName);
		if (sheet.xslName.contains("ONEOKRECAP") && snippet.up == 3
				&& snippet.left >= 22) {
			CellArray ca = new CellArray(3, true, snippet.left, snippet.right);
			ca.numberLabel = true;
			cas.add(ca);
		}

		// fix some wrong cell arrays.
		postProcessedCellArray(cas);

		return cas;
	}

	private void extract(CellArray ca, List<CellArray> cas, boolean first) {
		if (ca.start >= ca.end) {
			return;
		}

		int index = 0;
		while (true) {
			index = splitByInvalidCell(ca);
			if (index == ca.start) {
				// delete the first wrong cell
				ca.start = ca.start + 1;
			} else {
				break;
			}
		}

		if (index != -1) {
			CellArray left = new CellArray(ca.rowOrColumn, ca.isRowCA,
					ca.start, index - 1);
			extract(left, cas, first);

			CellArray right = new CellArray(ca.rowOrColumn, ca.isRowCA,
					index + 1, ca.end);
			extract(right, cas, first);
			return;
		} else {
			if (first && !hasFormula(ca)) {
				if (analysisPattern.curPattern == AnalysisPattern.Share_Cells) {
					if (isNumberLabel(ca)) {
						cas.add(ca);
					}
				}
			} else {
				specialExtract(ca, cas);
				return;
			}
		}
	}

	private boolean isNumberLabel(CellArray ca) {
		try {
			List<Integer> values = new ArrayList<Integer>();
			for (int i = 0; i < ca.size(); i++) {
				double tmp = Utils.getNumericalValue(ca.getCell(sheet, i));
				if (tmp - ((int) tmp) < 0.0000001) {
					values.add(((int) tmp));
				}
			}

			List<CellArray> newCAs = new ArrayList<CellArray>();
			int start = 0;
			while (start + 1 < ca.size()) {
				int off = values.get(start + 1) - values.get(start);
				if (off != 1 && off != -1) {
					start++;
					continue;
				}
				int end = start + 1;
				for (; end < values.size(); end++) {
					int cur = values.get(end);
					int pre = values.get(end - 1);
					if (cur - pre != off) {
						break;
					}
				}
				if (end - start >= 3) {
					CellArray newCA = new CellArray(ca.rowOrColumn, ca.isRowCA,
							ca.start + start, ca.start + end - 1);
					newCAs.add(newCA);
				}
				start = end;
			}

			start = 0;
			int end = 0;
			if (newCAs.size() > 0) {
				start = newCAs.get(0).start;
				end = newCAs.get(0).end;
			}
			for (int i = 1; i < newCAs.size(); i++) {
				CellArray cur = newCAs.get(i);
				CellArray pre = newCAs.get(i - 1);
				if (pre.end + 1 == cur.start) {
					end = cur.end;
				} else {
					break;
				}
			}
			if (ca.size() >= 4 && ca.start == start && ca.end == end) {
				ca.numberLabel = true;
				return true;
			}
		} catch (Exception e) {
		}
		return false;
	}

	private void specialExtract(CellArray ca, List<CellArray> cas) {
		if (ca.start >= ca.end) {
			return;
		}

		if (!hasFormula(ca)) {
			return;
		}

		try {
			int index = 0;
			while (true) {
				// catch some unexcepted exceptions
				index = splitCA(ca, cas);
				if (index == ca.start) {
					// delete the first wrong cell
					ca.start = ca.start + 1;
				} else {
					break;
				}
			}
			// succeed!
			if (index == -1) {
				return;
			} else {
				CellArray left = new CellArray(ca.rowOrColumn, ca.isRowCA,
						ca.start, index - 1);
				specialExtract(left, cas);

				CellArray right = new CellArray(ca.rowOrColumn, ca.isRowCA,
						index, ca.end);
				specialExtract(right, cas);
				return;

			}
		} catch (Exception e) {
			Log.logNewLine(e, Log.writer);
		}
	}

	protected abstract int splitCA(CellArray ca, List<CellArray> cas);

	protected boolean hasFormula(CellArray ca) {
		for (int i = 0; i < ca.size(); i++) {
			Cell cell = ca.getCell(sheet, i);
			if (cell != null && CellUtils.isFormula(cell)) {
				return true;
			}
		}
		return false;
	}

	private int splitByInvalidCell(CellArray ca) {
		for (int i = 0; i < ca.size(); i++) {
			Cell cell = ca.getCell(sheet, i);
			if (!Utils.isValidOutput(cell)) {
				return ca.start + i;
			}
			if (cell != null && CellUtils.isFormula(cell)) {
				String formula = cell.getCellFormula();
				if (FormulaUtils.isUnsupported(formula)) {
					return ca.start + i;
				}

				// Can't parse formulas.
				try {
					List<Object> pattern = ParseFormula.parse(ca, cell);
					ParseFormula.getR1C1Pattern(pattern);
				} catch (Exception e) {
					return ca.start + i;
				}
			}
			// We can't handle array formula now.
			if (cell.isPartOfArrayFormulaGroup()) {
				return ca.start + i;
			}
		}

		return -1;
	}

	private void postProcessedCellArray(List<CellArray> cas) {

		removeSubCellArray(cas);

		fixNumberLabelCellArray(cas);

		for (int i = 0; i < cas.size();) {
			CellArray ca = cas.get(i);

			// (1) If the cell array just have one element, we delete it.
			if (ca.end - ca.start < 1) {
				cas.remove(i);
				continue;
			}

			// (2) If the cell array just contain plain values, delete it.
			if (!hasFormula(ca)) {
				cas.remove(i);
				continue;
			}

			// (3) Has no input, only simple computations.
			if (hasNoInputs(ca)) {
				cas.remove(i);
				continue;
			}

			// (4) The cells are in a range. Combined cells
			if (isInCellRange(ca)) {
				cas.remove(i);
				continue;
			}

			boolean changed = false;
			// (3) delete the black cells in the beginning of the cell array
			while (ca.size() != 0) {
				Cell cell = ca.getCell(sheet, 0);

				if (!Utils.isValidOutput(cell)) {
					ca.start = ca.start + 1;
					changed = true;
				} else {
					break;
				}
			}

			// (4) delete the black cells in ending of the cell array
			while (ca.size() != 0) {
				Cell cell = ca.getCell(sheet, ca.size() - 1);

				if (!Utils.isValidOutput(cell)) {
					ca.end = ca.end - 1;
					changed = true;
				} else {
					break;
				}
			}

			// (5) If the cell array are separated by an empty cell (or
			// unprocessed cell), separate it
			// into two cell arrays. (this is wrong. 2014.09.22)
			// After separation, the cell array may only have plain values
			// In order to simplify the process, I still need to separate them.
			// Delete the cell array when only containing plain values.
			// (2014.10.18)
			Set<R1C1Cell> inputs = null;
			try {
				inputs = ConstraintGenerator.generateInputs(sheet, ca);
			} catch (Exception e) {
				Log.logNewLine(e, Log.writer);
				cas.remove(i);
				continue;
			}

			// delete the starting and ending cells with wrong inputs or output
			while (ca.size() != 0) {
				Cell cell = ca.getCell(sheet, 0);

				if (!Utils.isValidOutput(cell)
						|| !Utils.isValidInput(sheet, ca, inputs, cell)) {
					ca.start = ca.start + 1;
					changed = true;
				} else {
					break;
				}
			}
			while (ca.size() != 0) {
				Cell cell = ca.getCell(sheet, ca.size() - 1);

				if (!Utils.isValidOutput(cell)
						|| !Utils.isValidInput(sheet, ca, inputs, cell)) {
					ca.end = ca.end - 1;
					changed = true;
				} else {
					break;
				}
			}
			// cut the cell array when some wrongs in the between.
			for (int j = 0; j < ca.size(); j++) {
				Cell cell = ca.getCell(sheet, j);
				// the output or inputs may be string, '/' or '-', or others
				if (!Utils.isValidOutput(cell)
						|| !Utils.isValidInput(sheet, ca, inputs, cell)) {
					CellArray ca1 = new CellArray(ca.rowOrColumn, ca.isRowCA,
							ca.start, ca.start + j - 1);
					CellArray ca2 = new CellArray(ca.rowOrColumn, ca.isRowCA,
							ca.start + j + 1, ca.end);

					// This is fine, because constants don't change.
					ca1.constants = ca.constants;
					ca2.constants = ca.constants;

					cas.remove(i);
					cas.add(ca1);
					cas.add(ca2);
					changed = true;
					break;
				}
			}

			if (changed == false) {
				i++;
			}
		}

		removeRefChangeConstant(cas);

		removeMultiFunction(cas);
	}

	private void fixNumberLabelCellArray(List<CellArray> cas) {
		List<CellArray> labels = new ArrayList<CellArray>();
		for (int i = 0; i < cas.size();) {
			CellArray ca = cas.get(i);
			if (ca.numberLabel) {
				cas.remove(i);
				labels.add(ca);
			} else {
				i++;
			}
		}

		for (int i = 0; i < labels.size(); i++) {
			CellArray label = labels.get(i);
			for (int j = 0; j < cas.size(); j++) {
				CellArray ca = cas.get(j);
				if (CAOverlap.overlap(label, ca)) {
					ca.start = ca.start + 1;
				}
			}
		}
	}

	private void removeSubCellArray(List<CellArray> cas) {
		for (int i = 0; i < cas.size();) {
			CellArray ca = cas.get(i);
			boolean isSub = false;
			for (CellArray tmp : cas) {
				if (ca != tmp && ca.isRowCA == tmp.isRowCA
						&& ca.rowOrColumn == tmp.rowOrColumn) {
					if (ca.start >= tmp.start && ca.end <= tmp.end) {
						isSub = true;
						break;
					}
				}
			}
			if (isSub) {
				cas.remove(i);
			} else {
				i++;
			}
		}
	}

	private void removeMultiFunction(List<CellArray> cas) {
		for (int i = 0; i < cas.size();) {
			CellArray ca = cas.get(i);

			try {
				List<Component> comps = ConstraintGenerator
						.generateConstraints(sheet, ca).getComponents();
				boolean max = false;
				boolean min = false;
				boolean average = false;
				boolean sum = false;
				for (Component comp : comps) {
					if (comp instanceof MaxComponent) {
						max = true;
					} else if (comp instanceof MinComponent) {
						min = true;
					} else if (comp instanceof AverageComponent) {
						average = true;
					} else if (comp instanceof SumComponent) {
						sum = true;
					}
				}

				int numOfFun = 0;
				if (max) {
					numOfFun++;
				}
				if (min) {
					numOfFun++;
				}
				if (average) {
					numOfFun++;
				}
				if (sum) {
					numOfFun++;
				}

				if (numOfFun >= 2) {
					cas.remove(i);
				} else {
					i++;
				}
			} catch (Exception e) {
				Log.logNewLine(e, Log.writer);
				continue;
			}
		}
	}

	private void removeRefChangeConstant(List<CellArray> cas) {
		for (int i = 0; i < cas.size();) {
			CellArray ca = cas.get(i);

			try {
				boolean allChangedConstants = false;
				Set<R1C1Cell> inputs = ConstraintGenerator.generateInputs(
						sheet, ca);
				if (inputs.size() >= 2) {
					allChangedConstants = true;
					for (R1C1Cell input : inputs) {
						if (input.referChanged == false) {
							allChangedConstants = false;
							break;
						}
					}
				}

				if (allChangedConstants) {
					cas.remove(i);
				} else {
					i++;
				}
			} catch (Exception e) {
				Log.logNewLine(e, Log.writer);
				continue;
			}
		}
	}

	private boolean isInCellRange(CellArray ca) {
		// Some cells are in a cell range. We omit these.
		boolean in = false;
		for (int j = 0; j < sheet.getSheet().getNumMergedRegions(); j++) {
			CellRangeAddress cra = sheet.getSheet().getMergedRegion(j);
			int firstRow = cra.getFirstRow();
			int firstColumn = cra.getFirstColumn();
			int lastRow = cra.getLastRow();
			int lastColumn = cra.getLastColumn();

			if (ca.isRowCA) {
				if (ca.rowOrColumn < firstRow || ca.rowOrColumn > lastRow) {
					in = false;
				} else if (ca.start > lastColumn || ca.end < firstColumn) {
					in = false;
				} else {
					in = true;
					break;
				}
			} else {
				if (ca.rowOrColumn < firstColumn || ca.rowOrColumn > lastColumn) {
					in = false;
				} else if (ca.start > lastRow || ca.end < firstRow) {
					in = false;
				} else {
					in = true;
					break;
				}
			}
		}
		return in;
	}

	protected boolean hasNoInputs(CellArray ca) {
		// Cell arrays have no inputs.
		Set<R1C1Cell> inputs = null;
		try {
			inputs = ConstraintGenerator.generateInputs(sheet, ca);
			if (inputs.size() == 0) {
				return true;
			}
		} catch (Exception e) {
			Log.logNewLine(e, Log.writer);
			return true;
		}
		return false;
	}
}
