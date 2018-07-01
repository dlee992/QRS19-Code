package thirdparty.CACheck.cellarray.inference;

import java.io.BufferedWriter;
import java.util.List;

import thirdparty.CACheck.AMSheet;
import thirdparty.CACheck.cellarray.extract.CAResult;
import thirdparty.CACheck.cellarray.synthesis.PatternSynthesis;
import thirdparty.CACheck.formula.FormulaUtils;
import thirdparty.CACheck.util.Log;

public class FormulaInference {
	private AMSheet sheet = null;

	public FormulaInference(AMSheet sheet) {
		this.sheet = sheet;
	}

	public List<Object> synthesis(CAResult car, BufferedWriter writer) {
		Log.logNewLine(writer);
		Log.logNewLine(car.cellArray.toString(), writer);

		List<Object> pattern = null;

		SimpleRecover sr = new SimpleRecover(sheet);
		pattern = sr.doRecover(car, writer);

		// we only need to extract spec for ambiguous
		// cell arrays.
		if (car.isAmbiguous) {
			if (pattern != null) {
				car.isFixInStage1 = true;
			} else {
				if (FormulaUtils.isSupported(car.constraints.getComponents())) {
					// OnlyIOSynthesis thirdparty.synthesis = new OnlyIOSynthesis(sheet);
					PatternSynthesis synthesis = new PatternSynthesis(sheet);
					pattern = synthesis.doSynthesis(car, writer);
				}
			}

			pattern = doSimpleSelect(car, pattern);
		}

		car.pattern = pattern;
		return pattern;
	}

	// the synthesized formula may be wrong. Here we want to find one formula
	// from
	// existed formulas, which can cover more cells
	public List<Object> doSimpleSelect(CAResult car, List<Object> pattern) {
		Constraints cons = car.constraints;
		List<FormulaPattern> fps = cons.getFormulaPatterns();

		List<Object> curPattern = pattern;
		float maxCoverage = 0.0f;
		float maxCECoverage = 0.0f;
		if (pattern != null) {
			maxCoverage = new Coverage(sheet).computeCoverage(pattern, car);
			maxCECoverage = new Coverage(sheet).computeCECoverage(pattern, car);
		}

		for (FormulaPattern fp : fps) {
			float coverage = new Coverage(sheet).computeCoverage(fp.pattern,
					car);
			float ceCoverage = new Coverage(sheet).computeCECoverage(
					fp.pattern, car);
			if (coverage > maxCoverage || coverage == maxCoverage
					&& ceCoverage > maxCECoverage) {
				maxCoverage = coverage;
				maxCECoverage = ceCoverage;
				curPattern = fp.pattern;
			}
		}

		return curPattern;
	}
}
