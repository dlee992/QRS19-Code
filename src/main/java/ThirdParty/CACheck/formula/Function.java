package ThirdParty.CACheck.formula;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;

import ThirdParty.CACheck.AMSheet;
import ThirdParty.CACheck.CellArray;
import ThirdParty.CACheck.R1C1Cell;
import ThirdParty.synthesis.component.Component;

import com.microsoft.z3.ArithExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.IntExpr;
import com.microsoft.z3.RealExpr;
import com.microsoft.z3.Z3Exception;

public abstract class Function implements Serializable {
	
	public static final long serialVersionUID = 450;
	
	public String funName;
	public List<List<Object>> inputs = new ArrayList<List<Object>>();

	public static Function createFunction(String funName) {
		Function fun = null;
		if (funName.toLowerCase().equals("sum")) {
			fun = new SumFunction();
		} else if (funName.toLowerCase().equals("average")) {
			fun = new AverageFunction();
		} else if (funName.toLowerCase().equals("max")) {
			fun = new MaxFunction();
		} else if (funName.toLowerCase().equals("min")) {
			fun = new MinFunction();
		} else {
			throw new UnsupportedFunctionException();
		}

		fun.funName = funName.toUpperCase();
		return fun;
	}

	public abstract double getFucntionValue(AMSheet sheet, CellArray ca,
			Cell curCell);

	public abstract ArithExpr parseSpecReal(Context ctx,
			Map<R1C1Cell, RealExpr> cell2Var) throws Z3Exception;

	public abstract ArithExpr parseSpecInt(Context ctx,
			Map<R1C1Cell, IntExpr> inputs) throws Z3Exception;

	public abstract List<Component> getComponents();

	public String getA1Formula(int curRow, int curCol) {
		StringBuffer sb = new StringBuffer();
		sb.append(funName);
		sb.append("(");
		for (List<Object> input : inputs) {
			String para = ParseFormula.getA1Pattern(curRow, curCol, input);
			sb.append(para).append(",");
		}
		sb.deleteCharAt(sb.length() - 1);
		sb.append(")");

		return sb.toString();
	}

	public String getR1C1Formula() {
		StringBuffer sb = new StringBuffer();
		sb.append(funName);
		sb.append("(");
		for (List<Object> input : inputs) {
			String para = ParseFormula.getR1C1Pattern(input);
			sb.append(para).append(",");
		}
		sb.deleteCharAt(sb.length() - 1);
		sb.append(")");

		return sb.toString();
	}

	public String toString() {
		return getR1C1Formula();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Function) {
			return obj.toString().equals(toString());
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}
}
