package thirdparty.CACheck.cellarray.inference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import thirdparty.CACheck.R1C1Cell;
import thirdparty.CACheck.formula.Function;
import thirdparty.CACheck.formula.ParseFormula;
import thirdparty.synthesis.component.Component;
import thirdparty.synthesis.component.ConstantComponent;
import thirdparty.synthesis.component.DivComponent;
import thirdparty.synthesis.component.MinusComponent;
import thirdparty.synthesis.component.MultComponent;
import thirdparty.synthesis.component.PlusComponent;

public class FormulaPattern {
	public List<Object> pattern = null;

	public Map<R1C1Cell, Double> defaultValues = new HashMap<R1C1Cell, Double>();

	public FormulaPattern(List<Object> pattern) {
		this.pattern = pattern;
	}

	public static List<Component> getComponents(List<Object> pattern) {
		return new FormulaPattern(pattern).getComponents();
	}

	public List<Component> getComponents() {
		List<Component> comps = new ArrayList<Component>();

		if (pattern == null) {
			return comps;
		}

		for (Object o : pattern) {
			if (o instanceof R1C1Cell) {
				// no op
			} else if (o instanceof Double) {
				int tmp = ((Double) o).intValue();
				comps.add(new ConstantComponent(tmp));
			} else if (o instanceof Function) {
				Function fun = (Function) o;
				comps.addAll(fun.getComponents());
			} else {
				String op = (String) o;
				if (op.contains("+")) {
					comps.add(new PlusComponent());
				} else if (op.contains("-")) {
					comps.add(new MinusComponent());
				} else if (op.contains("*")) {
					comps.add(new MultComponent());
				} else if (op.contains("/")) {
					comps.add(new DivComponent());
				}
			}
		}

		return comps;
	}

	public static Set<R1C1Cell> getInputs(List<Object> pattern) {
		return new FormulaPattern(pattern).getInputs();
	}

	public Set<R1C1Cell> getInputs() {
		Set<R1C1Cell> inputs = new TreeSet<R1C1Cell>(R1C1Cell.getComparator());
		if (pattern == null) {
			return inputs;
		}
		for (Object o : pattern) {
			if (o instanceof R1C1Cell) {
				inputs.add((R1C1Cell) o);
			} else if (o instanceof Function) {
				Function f = (Function) o;
				for (List<Object> input : f.inputs) {
					for (Object i : input) {
						if (i instanceof R1C1Cell) {
							inputs.add((R1C1Cell) i);
						}
					}
				}
			}
		}

		return inputs;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(ParseFormula.getR1C1Pattern(pattern));
		sb.append("  --<Default Values>-- ");
		for (R1C1Cell cell : defaultValues.keySet()) {
			sb.append(cell);
			sb.append("=");
			sb.append(defaultValues.get(cell));
			sb.append(",");
		}
		if (defaultValues.size() > 0) {
			sb.deleteCharAt(sb.length() - 1);
		}
		return sb.toString();
	}
}
