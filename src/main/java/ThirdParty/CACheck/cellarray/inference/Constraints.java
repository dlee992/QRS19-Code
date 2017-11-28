package ThirdParty.CACheck.cellarray.inference;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import ThirdParty.CACheck.R1C1Cell;
import ThirdParty.synthesis.basic.IOPair;
import ThirdParty.synthesis.component.Component;

public class Constraints {
	private List<IOPair> ioPairs = new ArrayList<IOPair>();
	private Set<R1C1Cell> inputs = null;
	private List<Component> components = null;
	private List<FormulaPattern> fps = null;

	public void setIOPairs(List<IOPair> ioPairs) {
		this.ioPairs = ioPairs;

		for (int i = 0; i < ioPairs.size(); i++) {
			IOPair pair = ioPairs.get(i);
			for (int j = i + 1; j < ioPairs.size();) {
				if (pair.equals(ioPairs.get(j))) {
					ioPairs.remove(j);
				} else {
					j++;
				}
			}
		}
	}

	public List<IOPair> getIOPairs() {
		return ioPairs;
	}

	public void setInputs(Set<R1C1Cell> inputs) {
		this.inputs = inputs;
	}

	public Set<R1C1Cell> getInputs() {
		return inputs;
	}

	public void setComponents(List<Component> components) {
		this.components = components;
	}

	public List<Component> getComponents() {
		return components;
	}

	public void setFormulaPatterns(List<FormulaPattern> specs) {
		this.fps = specs;
	}

	public List<FormulaPattern> getFormulaPatterns() {
		return fps;
	}
}
