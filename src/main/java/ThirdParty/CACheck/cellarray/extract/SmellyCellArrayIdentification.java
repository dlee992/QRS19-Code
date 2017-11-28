package ThirdParty.CACheck.cellarray.extract;

import java.util.List;

import ThirdParty.CACheck.AMSheet;
import ThirdParty.CACheck.CellArray;
import ThirdParty.CACheck.cellarray.inference.FormulaPattern;
import ThirdParty.CACheck.cellarray.synthesis.SynthesisUtils;
import ThirdParty.CACheck.util.Utils;

public class SmellyCellArrayIdentification {

	private AMSheet sheet = null;

	public SmellyCellArrayIdentification(AMSheet sheet) {
		this.sheet = sheet;
	}

	public boolean isMissingFormulaSmell(CellArray ca) {
		if (Utils.hasPlainValue(sheet, ca)) {
			return true;
		}

		return false;
	}

	public boolean isInconsistentFormulaSmell(List<FormulaPattern> fps) {

		/*
		 * // we think refer change is an inconsistent cell arrays. for
		 * (FormulaPattern spec : fps) { if (Utils.referChanged(spec.pattern)) {
		 * return true; } }
		 */
		if (fps.size() > 1) {
			FormulaPattern fp1 = fps.get(0);
			for (int i = 1; i < fps.size(); i++) {
				FormulaPattern fp2 = fps.get(i);
				if (!SynthesisUtils.semanticEqual(fp1.pattern, fp2.pattern)) {
					return true;
				}
			}
		}

		return false;
	}
}
