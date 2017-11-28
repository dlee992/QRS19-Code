package ThirdParty.CACheck.cellarray.synthesis;

import java.util.ArrayList;
import java.util.List;

import ThirdParty.CACheck.cellarray.inference.Constraints;
import ThirdParty.CACheck.cellarray.inference.FormulaPattern;

public class SpecClassify {
	public static List<List<FormulaPattern>> classify(Constraints cons) {
		List<FormulaPattern> fps = cons.getFormulaPatterns();

		List<List<FormulaPattern>> groups = new ArrayList<List<FormulaPattern>>();

		boolean[] classified = new boolean[fps.size()];
		for (int i = 0; i < classified.length; i++) {
			if (classified[i] == false) {
				List<FormulaPattern> group = new ArrayList<FormulaPattern>();
				group.add(fps.get(i));
				classified[i] = true;
				for (int j = 0; j < fps.size(); j++) {
					if (group.indexOf(fps.get(j)) == -1) {
						boolean compatible = true;
						for (FormulaPattern fp : group) {
							if (!SynthesisUtils
									.compatible(cons, fps.get(j), fp)) {
								compatible = false;
								break;
							}
						}
						if (compatible) {
							group.add(fps.get(j));
							classified[j] = true;
						}
					}
				}

				groups.add(group);
			}
		}

		return groups;
	}
}
