package thirdparty.CACheck.cellarray.inference;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;

import thirdparty.CACheck.AMSheet;
import thirdparty.CACheck.CellArray;
import thirdparty.CACheck.R1C1Cell;
import thirdparty.CACheck.cellarray.synthesis.SynthesisUtils;
import thirdparty.CACheck.formula.ParseFormula;
import thirdparty.CACheck.util.CellUtils;
import thirdparty.CACheck.util.Utils;
import thirdparty.synthesis.basic.IOPair;
import thirdparty.synthesis.component.Component;
import thirdparty.synthesis.component.Components;
import thirdparty.synthesis.component.NumComponent;
import thirdparty.synthesis.component.PlusComponent;
import thirdparty.synthesis.component.SumComponent;

public class ConstraintGenerator {

	public static Constraints generateConstraints(AMSheet sheet, CellArray ca) {
		Constraints cons = new Constraints();

		Set<R1C1Cell> inputs = generateInputs(sheet, ca);
		List<FormulaPattern> fps = generateFormulaPatterns(sheet, ca, inputs);
		List<IOPair> ioPairs = generateIOPairs(sheet, ca, inputs);
		List<Component> components = generateComponents(fps, inputs);
		cons.setInputs(inputs);
		cons.setIOPairs(ioPairs);
		cons.setComponents(components);
		cons.setFormulaPatterns(fps);

		return cons;
	}

	public static Set<R1C1Cell> generateInputs(AMSheet sheet, CellArray ca) {
		Set<R1C1Cell> inputs = new HashSet<R1C1Cell>();

		for (int index = 0; index < ca.size(); index++) {
			Cell cell = ca.getCell(sheet, index);
			if (cell != null && CellUtils.isFormula(cell)) {
				List<Object> pattern = ParseFormula.parse(ca, cell);
				if (pattern != null) {
					inputs.addAll(FormulaPattern.getInputs(pattern));
				}
			}
		}

		return inputs;
	}

	public static List<FormulaPattern> simplifyFormulaPatterns(
			List<FormulaPattern> fps) {
		List<FormulaPattern> newFps = new ArrayList<FormulaPattern>();
		newFps.addAll(fps);
		// delete the duplicated formulas.
		for (int i = 0; i < newFps.size(); i++) {
			FormulaPattern fp1 = newFps.get(i);
			for (int j = i + 1; j < newFps.size(); j++) {
				FormulaPattern fp2 = newFps.get(j);
				if (SynthesisUtils.semanticEqual(fp1.pattern, fp2.pattern)) {
					newFps.remove(j);
					j--;
				}
			}
		}

		return newFps;
	}

	private static List<FormulaPattern> generateFormulaPatterns(AMSheet sheet,
			CellArray ca, Set<R1C1Cell> inputs) {
		List<FormulaPattern> fps = new ArrayList<FormulaPattern>();
		for (int index = 0; index < ca.size(); index++) {
			Cell cell = ca.getCell(sheet, index);
			if (cell != null && CellUtils.isFormula(cell)) {
				List<Object> pattern = ParseFormula.parse(ca, cell);
				if (pattern != null) {
					FormulaPattern fp = new FormulaPattern(pattern);
					Set<R1C1Cell> pi = fp.getInputs();
					for (R1C1Cell input : inputs) {
						if (!pi.contains(input)) {
							// get the current data.
							double inputData = 0;
							try {
								inputData = Utils.getNumericalValue(sheet, ca,
										input, cell);
							} catch (Exception e) {
							}
							fp.defaultValues.put(input, inputData);
						}
					}

					boolean existed = false;
					for (int i = 0; i < fps.size(); i++) {
						FormulaPattern tmp = fps.get(i);
						// same formula patterns
						if (sameFormulaPatterns(tmp, fp)) {
							existed = true;
							break;
						}
					}
					if (!existed) {
						fps.add(fp);
					}
				}
			}
		}

		return fps;
	}

	private static boolean sameFormulaPatterns(FormulaPattern fp1,
			FormulaPattern fp2) {

		if (fp1.defaultValues.size() != fp2.defaultValues.size()) {
			return false;
		}

		for (R1C1Cell cell : fp1.defaultValues.keySet()) {
			Double v1 = fp1.defaultValues.get(cell);
			Double v2 = fp2.defaultValues.get(cell);
			if (v1 == null || v2 == null || !v1.equals(v2)) {
				return false;
			}
		}

		if (!SynthesisUtils.semanticEqual(fp1.pattern, fp2.pattern)) {
			return false;
		}

		return true;
	}

	private static List<IOPair> generateIOPairs(AMSheet sheet, CellArray ca,
			Set<R1C1Cell> inputs) {

		List<IOPair> ioPairs = new ArrayList<IOPair>();

		for (int i = 0; i < ca.size(); i++) {
			Cell curCell = ca.getCell(sheet, i);

			List<Integer> ins = new ArrayList<Integer>();
			double outputData = 0.0;
			try {
				for (R1C1Cell input : inputs) {
					double inputData = Utils.getNumericalValue(sheet, ca,
							input, curCell);
					ins.add((int) inputData);
				}
				outputData = Utils.getNumericalValue(curCell);
			} catch (Exception e) {
				continue;
			}

			IOPair pair = new IOPair(ins.toArray(new Integer[0]),
					(int) outputData);

			boolean existed = false;
			for (int j = 0; j < ioPairs.size(); j++) {
				IOPair tmp = ioPairs.get(j);
				if (pair.equals(tmp)) {
					existed = true;
					break;
				}
			}
			if (!existed) {
				ioPairs.add(pair);
			}
		}

		return ioPairs;
	}

	private static List<Component> generateComponents(List<FormulaPattern> fps,
			Set<R1C1Cell> inputs) {

		List<FormulaPattern> newFps = simplifyFormulaPatterns(fps);

		List<Component> comps = new ArrayList<Component>();

		for (FormulaPattern fp : newFps) {
			if (fp.pattern.size() == 1) {
				comps.add(new PlusComponent());
				// components.add(Components.getComponent(Components.MULT));
			}

			comps.addAll(fp.getComponents());
		}

		// post-process
		boolean hasSum = false;
		int biggestSum = 0;
		int plusNum = 0;

		for (int i = 0; i < comps.size(); i++) {
			Component comp = comps.get(i);
			if (comp instanceof NumComponent) {
				NumComponent nc = (NumComponent) comp;
				if (nc.getInputNum() <= 1) {
					comps.remove(i);
					i--;
				}
			}
		}

		for (Component comp : comps) {
			if (comp.type == Components.PLUS) {
				plusNum++;
			}
			if (comp.type == Components.SUM) {
				hasSum = true;
				int tmp = ((SumComponent) comp).getInputNum();
				if (biggestSum < tmp) {
					biggestSum = tmp;
				}
			}
		}

		if (hasSum && biggestSum + plusNum < inputs.size()) {
			for (int i = 1; i <= inputs.size() - biggestSum - plusNum; i++) {
				comps.add(new PlusComponent());
			}
		}

		return comps;
	}
}
