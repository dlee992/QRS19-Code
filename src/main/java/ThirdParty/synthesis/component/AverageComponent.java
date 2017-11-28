package ThirdParty.synthesis.component;

import ThirdParty.synthesis.basic.IOType;
import ThirdParty.synthesis.basic.Type;
import ThirdParty.synthesis.basic.VarType;

import com.microsoft.z3.ArithExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.RealExpr;
import com.microsoft.z3.Z3Exception;

public final class AverageComponent extends NumComponent {

	public AverageComponent(int inputNum) {
		this(inputNum, VarType.INTEGER);
	}

	public AverageComponent(int inputNum, VarType vt) {
		this.inputNum = inputNum;
		type = Components.AVERAGE;
		varType = vt;
		compId++;
		switchable = true;

		Type inType = new Type(vt, IOType.COMP_INPUT);
		Type outType = new Type(vt, IOType.COMP_OUTPUT);
		for (int i = 0; i < inputNum; i++) {
			varTypes.add(inType);
		}
		varTypes.add(outType);
	}

	public void init(Context ctx) throws Z3Exception {
		super.init(ctx);

		Expr res = variables.get(inputNum);

		ArithExpr sum = ctx.mkInt(0);
		for (int i = 0; i < inputNum; i++) {
			sum = ctx.mkAdd(sum, (ArithExpr) variables.get(i));
		}

		RealExpr tmp = (RealExpr) ctx.mkDiv(sum,
				ctx.mkReal(new Double(inputNum).toString()));
		Expr average = tmp;
		if (varType == VarType.INTEGER) {
			average = ctx.mkReal2Int(tmp);
		}

		spec = ctx.mkEq(average, res);
	}

	public String getProg(String[] paras) {
		StringBuffer sb = new StringBuffer();
		sb.append("AVERAGE(");
		for (int i = 0; i < paras.length - 1; i++) {
			sb.append(paras[i] + ",");
		}
		sb.append(paras[paras.length - 1]);
		sb.append(")");
		return sb.toString();
	}

	public boolean equals(Object obj) {
		if (obj instanceof AverageComponent) {
			if (((AverageComponent) obj).getInputNum() == this.inputNum) {
				return true;
			}
		}
		return false;
	}

}
