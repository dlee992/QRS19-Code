package thirdparty.CACheck.cellarray.inference;

import java.io.BufferedWriter;
import java.util.List;
import java.util.Set;

import thirdparty.CACheck.AMSheet;
import thirdparty.CACheck.R1C1Cell;
import thirdparty.CACheck.cellarray.extract.CAResult;
import thirdparty.CACheck.cellarray.synthesis.SpecClassify;
import thirdparty.CACheck.cellarray.synthesis.SynthesisUtils;
import thirdparty.CACheck.util.Log;

public class SimpleRecover {
	private AMSheet sheet = null;

	public SimpleRecover(AMSheet sheet) {
		this.sheet = sheet;
		this.sheet.getClass();
	}

	public List<Object> doRecover(CAResult car, BufferedWriter writer) {

		Constraints cons = car.constraints;
		List<FormulaPattern> fps = cons.getFormulaPatterns();

		if (fps.size() == 0) {
			//TODO
			return null;
//			throw new RuntimeException(
//					"Sorry, the cell array does not have any formula!");
		}

		if (fps.size() == 1) {
			return fps.get(0).pattern;
		}

		List<List<FormulaPattern>> groups = SpecClassify.classify(cons);

		if (groups.size() == 1) {
			return doGroupRecover(car, groups.get(0), writer);
		} else {
			// the specifications are not compatible
			return null;
		}
	}

	public List<Object> doGroupRecover(CAResult car,
			List<FormulaPattern> group, BufferedWriter writer) {

		Log.logNewLine(writer);
		Log.logNewLine("Simple Group Recover starting....", writer);
		SynthesisUtils.printFormulaPatterns(group, writer);

		// TODO, we try to improve performance.
		// If we ignore this, the result should be more precise.
		if (group.size() == 1) {
			return group.get(0).pattern;
		}

		Set<R1C1Cell> inputs = car.constraints.getInputs();
		for (FormulaPattern fp : group) {
			if (fp.getInputs().size() == inputs.size()) {
				return fp.pattern;
			}
		}

		return null;
	}
}
