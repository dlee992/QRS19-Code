package thirdparty.CACheck.cellarray.extract;

import java.io.BufferedWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.poi.ss.usermodel.Cell;

import thirdparty.CACheck.AMSheet;
import thirdparty.CACheck.CellArray;
import thirdparty.CACheck.R1C1Cell;
import thirdparty.CACheck.amcheck.AnalysisPattern;
import thirdparty.CACheck.cellarray.inference.ConstraintGenerator;
import thirdparty.CACheck.cellarray.inference.Coverage;
import thirdparty.CACheck.cellarray.inference.FormulaInference;
import thirdparty.CACheck.cellarray.inference.FormulaPattern;
import thirdparty.CACheck.cellarray.synthesis.SynthesisUtils;
import thirdparty.CACheck.formula.ParseFormula;
import thirdparty.CACheck.snippet.Snippet;
import thirdparty.CACheck.util.CellUtils;
import thirdparty.CACheck.util.Log;
import thirdparty.CACheck.util.Utils;

public class AmbiguousDetector {
	private AMSheet sheet = null;
	private List<Snippet> allSnippets = null;

	public AmbiguousDetector(AMSheet sheet, List<Snippet> allSnippets) {
		this.sheet = sheet;
		this.allSnippets = allSnippets;
	}

	public void detect(CAResult car, BufferedWriter writer, AnalysisPattern analysisPattern) {

		CellArray ca = car.cellArray;

		if (isSameRowOrColumn(ca, car.pattern)) {
			car.isSameRowOrColumn = true;
		}

		if (!car.isSameRowOrColumn
				&& analysisPattern.curPattern != AnalysisPattern.Correct_CellArray) {
			fixTransitiveCellArray(car, car.pattern, writer);
			fixSelfRefCellArray(car, car.pattern);

			fixWrongInputCell(car, car.pattern);
		}

		// delete the cell array only have one cell.
		if (car.cellArray.size() <= 1) {
			//TODO:
			return;
			//throw new RuntimeException("Only have one element!");
		}

		for (int i = 0; i < ca.size(); i++) {
			Cell cell = ca.getCell(sheet, i);

			if (isConformanceError(cell, ca, car.pattern)) {
				car.errorCells.add(cell);
			}
			if (isCorrect(cell, ca, car.pattern)) {
				car.correctCells.add(cell);
			} else if (isMissing(cell, ca, car.pattern)) {
				car.missingCells.add(cell);
				car.ambiguousCells.add(cell);
			} else if (isSmellyFormula(cell, ca, car.pattern)) {
				car.smellyFormulaCells.add(cell);
				car.ambiguousCells.add(cell);
			}
			if (isReferChanged(cell, ca)) {
				car.referChangedCells.add(cell);
			}

			if (isPlainValue(cell, ca)) {
				car.plainValueCells.add(cell);
			}
			if (isInconsistentFormula(cell, ca, car.pattern)) {
				car.inconsistentFormulaCells.add(cell);
			}
		}

		// We can revise some results when cells are all correct.
		if (car.correctCells.size() == car.cellArray.size()) {
			car.isAmbiguous = false;
			car.isInconsistent = false;
			car.isMissing = false;
			car.isFixInStage1 = false;
		}

		// add two other information
		if (isSameRowOrColumn(ca, car.pattern)) {
			car.isSameRowOrColumn = true;
		}
		if (hasConstants(ca, car.pattern)) {
			car.hasConstants = true;
		}

		car.oppositeInputs = oppositeInputs(car, car.pattern, true);

		if (car.isAmbiguous) {
			// TODO For normal coverage and ce coverage, I think they should be
			// same.
			// If the cells' value can be computed, or the table is blank, then
			// normal coverage is correct too.
			float percentage = new Coverage(sheet).computeCECoverage(
					car.pattern, car);

			car.percentage = percentage;
		}
	}

	// when the cell array's cell is out of range, remove it.
	private void fixWrongInputCell(CAResult car, List<Object> pattern) {

		boolean changed = false;
		while (car.cellArray.size() > 1) {
			if (outOfRange(car.cellArray, 0, pattern)
					|| emptyInputs(car.cellArray, 0, pattern)) {
				car.cellArray.start = car.cellArray.start + 1;
				changed = true;
			} else {
				break;
			}
		}

		while (car.cellArray.size() > 1) {
			if (outOfRange(car.cellArray, car.cellArray.size() - 1, pattern)
					|| emptyInputs(car.cellArray, car.cellArray.size() - 1,
							pattern)) {
				car.cellArray.end = car.cellArray.end - 1;
				changed = true;
			} else {
				break;
			}
		}

		if (changed) {
			SmellyCellArrayIdentification smellyCAExtract = new SmellyCellArrayIdentification(
					sheet);
			if (smellyCAExtract.isMissingFormulaSmell(car.cellArray)) {
				car.isMissing = true;
			} else {
				car.isMissing = false;
			}
			if (smellyCAExtract.isInconsistentFormulaSmell(car.constraints
					.getFormulaPatterns())) {
				car.isInconsistent = true;
			} else {
				car.isInconsistent = false;
			}
			if (car.isMissing || car.isInconsistent) {
				car.isAmbiguous = true;
			} else {
				car.isAmbiguous = false;
			}
		}
	}

	private boolean emptyInputs(CellArray ca, int i, List<Object> pattern) {
		Cell curCell = ca.getCell(sheet, i);
		if (curCell.getCellType() != Cell.CELL_TYPE_NUMERIC) {
			return false;
		}
		Set<R1C1Cell> inputs = FormulaPattern.getInputs(pattern);
		int emptyInputs = 0;
		for (R1C1Cell inputCell : inputs) {
			R1C1Cell trueInput = ca.getTrueInput(curCell, inputCell);
			Cell cell = sheet.getCell(trueInput.row, trueInput.column);
			if (CellUtils.isBlank(cell)) {
				emptyInputs++;
			}
		}

		if (emptyInputs == inputs.size()) {
			return true;
		} else {
			return false;
		}
	}

	private boolean outOfRange(CellArray ca, int i, List<Object> pattern) {
		Set<R1C1Cell> inputs = FormulaPattern.getInputs(pattern);
		int OORNum = 0;
		int ConNum = 0;
		for (R1C1Cell inputCell : inputs) {
			if (!inputCell.rowRelative && !inputCell.columnRelative) {
				ConNum++;
			} else if (outOfRange(ca, i, inputCell)) {
				OORNum++;
			}
		}

		if (ConNum != inputs.size() && ConNum + OORNum == inputs.size()) {
			return true;
		} else {
			return false;
		}
	}

	private boolean outOfRange(CellArray ca, int i, R1C1Cell inputCell) {
		int curRow = ca.isRowCA ? ca.rowOrColumn : ca.start + i;
		int curColumn = ca.isRowCA ? ca.start + i : ca.rowOrColumn;
		inputCell = inputCell.getTrueCell(curRow, curColumn);
		boolean outOfRange = true;
		for (Snippet snippet : allSnippets) {
			if (inputCell.row >= snippet.up && inputCell.row <= snippet.bottom
					&& inputCell.column >= snippet.left
					&& inputCell.column <= snippet.right) {
				outOfRange = false;
				break;
			}
		}
		return outOfRange;
	}

	// when the cell array is transitive, delete the first cells with empty
	// inputs.
	private void fixTransitiveCellArray(CAResult car, List<Object> pattern, BufferedWriter writer) {
		boolean changed = false;

		// RC[-1]
		int origStart = car.cellArray.start;
		int transIndex = -1;
		while (true) {
			R1C1Cell inputR1C1Cell = getTransitiveCell(car.cellArray.isRowCA,
					pattern, transIndex);
			if (inputR1C1Cell != null && inputR1C1Cell.rowRelative
					&& inputR1C1Cell.columnRelative) {
				int curRow = car.cellArray.isRowCA ? car.cellArray.rowOrColumn
						: car.cellArray.start;
				int curColumn = car.cellArray.isRowCA ? car.cellArray.start
						: car.cellArray.rowOrColumn;
				inputR1C1Cell = inputR1C1Cell.getTrueCell(curRow, curColumn);
				Cell inputCell = sheet.getCell(inputR1C1Cell.row,
						inputR1C1Cell.column);
				if (!car.isAmbiguous) {
					if (!outOfRange(car.cellArray, 0, inputR1C1Cell)) {
						car.cellArray.extendStart = car.cellArray.start
								+ transIndex;
					}
				} else if (outOfRange(car.cellArray, 0, inputR1C1Cell)
						|| Utils.isEmpty(inputCell)) {
					car.cellArray.start = car.cellArray.start + 1;
					changed = true;
				}
			} else {
				break;
			}
			transIndex--;
		}

		if (changed && car.cellArray.extendStart == -1) {
			car.cellArray.extendStart = origStart;
		}

		// RC[1]
		transIndex = 1;
		while (true) {
			R1C1Cell inputR1C1Cell = getTransitiveCell(car.cellArray.isRowCA,
					pattern, transIndex);
			if (inputR1C1Cell != null && inputR1C1Cell.rowRelative
					&& inputR1C1Cell.columnRelative) {
				int curRow = car.cellArray.isRowCA ? car.cellArray.rowOrColumn
						: car.cellArray.end;
				int curColumn = car.cellArray.isRowCA ? car.cellArray.end
						: car.cellArray.rowOrColumn;
				inputR1C1Cell = inputR1C1Cell.getTrueCell(curRow, curColumn);
				Cell inputCell = sheet.getCell(inputR1C1Cell.row,
						inputR1C1Cell.column);
				if (!car.isAmbiguous) {
					if (!outOfRange(car.cellArray, car.cellArray.size() - 1,
							inputR1C1Cell)) {
						// car.cellArray.extendStart = car.cellArray.start
						// + transIndex;
					}
				} else if (outOfRange(car.cellArray, car.cellArray.size() - 1,
						inputR1C1Cell) || Utils.isEmpty(inputCell)) {
					car.cellArray.end = car.cellArray.end - 1;
					changed = true;
				}
			} else {
				break;
			}
			transIndex++;
		}

		if (changed) {
			SmellyCellArrayIdentification smellyCAExtract = new SmellyCellArrayIdentification(
					sheet);
			
			car.constraints = ConstraintGenerator.generateConstraints(
					sheet, car.cellArray);
			
			try {
				FormulaInference fs = new FormulaInference(sheet);
				fs.synthesis(car, writer);
			} catch (Exception e) {
				Log.logNewLine(e, writer);
			}
			
			if (smellyCAExtract.isMissingFormulaSmell(car.cellArray)) {
				car.isMissing = true;
			} else {
				car.isMissing = false;
			}
			if (smellyCAExtract.isInconsistentFormulaSmell(car.constraints
					.getFormulaPatterns())) {
				car.isInconsistent = true;
			} else {
				car.isInconsistent = false;
			}
			if (car.isMissing || car.isInconsistent) {
				car.isAmbiguous = true;
			} else {
				car.isAmbiguous = false;
			}
		}
	}

	// when the cell array's first cell ref itselft, delete the first cell.
	private void fixSelfRefCellArray(CAResult car, List<Object> pattern) {
		boolean changed = false;

		int origStart = car.cellArray.start;
		while (true) {
			boolean needFix = false;
			int curRow = car.cellArray.isRowCA ? car.cellArray.rowOrColumn
					: car.cellArray.start;
			int curColumn = car.cellArray.isRowCA ? car.cellArray.start
					: car.cellArray.rowOrColumn;
			for (R1C1Cell inputR1C1Cell : FormulaPattern.getInputs(pattern)) {
				inputR1C1Cell = inputR1C1Cell.getTrueCell(curRow, curColumn);
				if (inputR1C1Cell.row == curRow
						&& inputR1C1Cell.column == curColumn) {
					car.cellArray.start = car.cellArray.start + 1;
					changed = true;
					needFix = true;
					break;
				}
			}
			if (needFix == false || car.cellArray.start >= car.cellArray.end) {
				break;
			}
		}

		if (changed && car.cellArray.extendStart == -1) {
			car.cellArray.extendStart = origStart;
		}

		if (changed) {
			SmellyCellArrayIdentification smellyCAExtract = new SmellyCellArrayIdentification(
					sheet);
			if (smellyCAExtract.isMissingFormulaSmell(car.cellArray)) {
				car.isMissing = true;
			} else {
				car.isMissing = false;
			}
			if (smellyCAExtract.isInconsistentFormulaSmell(car.constraints
					.getFormulaPatterns())) {
				car.isInconsistent = true;
			} else {
				car.isInconsistent = false;
			}
			if (car.isMissing || car.isInconsistent) {
				car.isAmbiguous = true;
			} else {
				car.isAmbiguous = false;
			}
		}
	}

	private R1C1Cell getTransitiveCell(boolean isRow, List<Object> pattern,
			int offset) {
		for (R1C1Cell input : FormulaPattern.getInputs(pattern)) {
			int curOffset = 0;
			if (isRow) {
				curOffset = input.column - input.curColumn;
			} else {
				curOffset = input.row - input.curRow;
			}

			if (curOffset == offset) {
				return input;
			}
		}

		return null;
	}

	private boolean isConformanceError(Cell cell, CellArray ca,
			List<Object> pattern) {
		// TODO
		// if the cell is empty/black, we thought it is right.(2014-10-18)
		if (Utils.isValidOutput(cell)) {
			if (SynthesisUtils.isError(sheet, ca, cell, pattern)) {
				return true;
			}
		}
		return false;
	}

	private boolean isCorrect(Cell cell, CellArray ca, List<Object> pattern) {

		if (!Utils.isValidOutput(cell)) {
			return false;
		}
		if (!CellUtils.isFormula(cell)) {
			return false;
		}

		List<Object> cellPattern = ParseFormula.parse(ca, cell);
		if (cellPattern == null) {
			return false;
		}
		if (SynthesisUtils.semanticEqual(cell, cellPattern, pattern)) {
			return true;
		}

		return false;
	}

	private boolean isMissing(Cell cell, CellArray ca, List<Object> pattern) {

		if (!Utils.isValidOutput(cell)) {
			return false;
		}
		if (!CellUtils.isFormula(cell)) {
			return true;
		}

		return false;
	}

	private boolean isSmellyFormula(Cell cell, CellArray ca,
			List<Object> pattern) {

		if (!Utils.isValidOutput(cell)) {
			return false;
		}
		if (!CellUtils.isFormula(cell)) {
			return false;
		}

		List<Object> cellPattern = ParseFormula.parse(ca, cell);
		if (cellPattern == null) {
			return true;
		}
		if (!SynthesisUtils.semanticEqual(cellPattern, pattern)) {
			return true;
		}

		return false;
	}

	private boolean isReferChanged(Cell cell, CellArray ca) {

		if (!Utils.isValidOutput(cell)) {
			return false;
		}
		if (!CellUtils.isFormula(cell)) {
			return false;
		}

		List<Object> cellPattern = ParseFormula.parse(ca, cell);

		if (cellPattern == null) {
			return false;
		}

		if (Utils.referChanged(cellPattern)) {
			return true;
		}

		return false;
	}

	private boolean isPlainValue(Cell cell, CellArray ca) {

		if (!Utils.isValidOutput(cell)) {
			return false;
		}
		if (CellUtils.isFormula(cell)) {
			return false;
		}

		return true;
	}

	private boolean isInconsistentFormula(Cell cell, CellArray ca,
			List<Object> pattern) {

		if (!Utils.isValidOutput(cell)) {
			return false;
		}
		if (!CellUtils.isFormula(cell)) {
			return false;
		}

		List<Object> cellPattern = ParseFormula.parse(ca, cell);
		if (cellPattern == null) {
			return false;
		}
		if (SynthesisUtils.semanticEqual(cellPattern, pattern)) {
			return false;
		}

		return true;
	}

	public boolean isSameRowOrColumn(CellArray ca, List<Object> pattern) {
		Set<R1C1Cell> inputs = FormulaPattern.getInputs(pattern);
		for (R1C1Cell cell : inputs) {
			if (cell.rowRelative || cell.columnRelative) {
				if (ca.isRowCA) {
					// R[*]C
					if (cell.column != cell.curColumn) {
						return false;
					}
				} else {
					// RC[*]
					if (cell.row != cell.curRow) {
						return false;
					}
				}
			} else {
				// Here for constants.
				return false;
			}
		}

		return true;
	}

	private boolean hasConstants(CellArray ca, List<Object> pattern) {
		Set<R1C1Cell> inputs = FormulaPattern.getInputs(pattern);
		for (R1C1Cell cell : inputs) {
			if (!cell.rowRelative && !cell.columnRelative || cell.referChanged) {
				return true;
			}
		}

		return false;
	}

	// we can use these to remove opposite cell arrays.
	public static int oppositeInputs(CAResult car, List<Object> pattern,
			boolean fixed) {

		Set<R1C1Cell> inputs = null;
		if (fixed) {
			inputs = FormulaPattern.getInputs(pattern);
		} else {
			inputs = car.constraints.getInputs();
		}
		Map<Integer, Set<R1C1Cell>> map = new HashMap<Integer, Set<R1C1Cell>>();

		for (R1C1Cell cell : inputs) {
			if (cell.rowRelative && cell.columnRelative
					|| car.constraints.getFormulaPatterns().size() >= 2
					&& car.constraints.getFormulaPatterns().size() <= 4
					&& cell.referChanged) {
				Integer key = null;
				if (car.cellArray.isRowCA) {
					key = cell.row;
				} else {
					key = cell.column;
				}

				if (map.get(key) == null) {
					Set<R1C1Cell> cells = new TreeSet<R1C1Cell>(
							R1C1Cell.getComparator());
					cells.add(cell);
					map.put(key, cells);
				} else {
					Set<R1C1Cell> cells = map.get(key);
					cells.add(cell);
					map.put(key, cells);
				}
			}
		}

		int max = 0;
		for (Set<R1C1Cell> cells : map.values()) {
			// use the conservative results.
			int size = cells.size();
			if (size - 1 > max) {
				// max = size;
			}

			// use accumulative results
			size = cells.size();
			max += size - 1;
		}

		return max;
	}

	// we can use these to remove opposite cell arrays.
	public int oppositeInputs_bak(CellArray ca, List<Object> pattern) {
		boolean posRowCA = true;
		int posRowCAInput = 0;
		boolean posColumnCA = true;
		int posColumnCAInput = 0;

		Set<R1C1Cell> inputs = FormulaPattern.getInputs(pattern);
		for (R1C1Cell cell : inputs) {
			if (cell.rowRelative && cell.columnRelative) {
				// R[*]C
				if (cell.column == cell.curColumn) {
					posRowCAInput++;
				} else {
					posRowCA = false;
					break;
				}
			}
		}

		for (R1C1Cell cell : inputs) {
			if (cell.rowRelative && cell.columnRelative) {
				// RC[*]
				if (cell.row == cell.curRow) {
					posColumnCAInput++;
				} else {
					posColumnCA = false;
					break;
				}
			}
		}

		if (ca.isRowCA && posColumnCA) {
			return posColumnCAInput;
		}
		if (!ca.isRowCA && posRowCA) {
			return posRowCAInput;
		}

		return 0;
	}
}
