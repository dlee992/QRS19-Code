package thirdparty.CACheck.formula;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;

import thirdparty.CACheck.AMSheet;
import thirdparty.CACheck.CellArray;
import thirdparty.CACheck.R1C1Cell;
import thirdparty.CACheck.cellarray.inference.FormulaPattern;
import thirdparty.CACheck.cellarray.synthesis.SynthesisUtils;
import thirdparty.synthesis.component.AverageComponent;
import thirdparty.synthesis.component.Component;

import com.microsoft.z3.ArithExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.IntExpr;
import com.microsoft.z3.RealExpr;
import com.microsoft.z3.Z3Exception;

public class AverageFunction extends Function {

	public static final long serialVersionUID = 451;
	
	public double getFucntionValue(AMSheet sheet, CellArray ca, Cell curCell) {
		Double data = 0.0;
		for (List<Object> input : inputs) {
			try {
				data += SynthesisUtils.computeValue(sheet, ca, curCell, input);
			} catch (Exception e) {
			}
		}
		return data / inputs.size();
	}

	public ArithExpr parseSpecReal(Context ctx, Map<R1C1Cell, RealExpr> cell2Var)
			throws Z3Exception {
		ArithExpr result = null;
		if (inputs.size() >= 1) {
			result = SynthesisUtils.parsePatternReal(ctx, inputs.get(0), cell2Var);
		}
		for (int i = 1; i < inputs.size(); i++) {
			result = ctx.mkAdd(result,
					SynthesisUtils.parsePatternReal(ctx, inputs.get(i), cell2Var));
		}

		result = ctx.mkDiv(result, ctx.mkInt(inputs.size()));
		return result;
	}

	public ArithExpr parseSpecInt(Context ctx, Map<R1C1Cell, IntExpr> cell2Var)
			throws Z3Exception {
		ArithExpr result = null;
		if (inputs.size() >= 1) {
			result = SynthesisUtils.parsePatternInt(ctx, inputs.get(0), cell2Var);
		}
		for (int i = 1; i < inputs.size(); i++) {
			result = ctx.mkAdd(result,
					SynthesisUtils.parsePatternInt(ctx, inputs.get(i), cell2Var));
		}

		// integer division.
		result = ctx.mkDiv(result, ctx.mkInt(inputs.size()));
		return result;
	}

	public List<Component> getComponents() {
		List<Component> comps = new ArrayList<Component>();
		comps.add(new AverageComponent(inputs.size()));
		for (List<Object> para : inputs) {
			comps.addAll(FormulaPattern.getComponents(para));
		}
		return comps;
	}
}
