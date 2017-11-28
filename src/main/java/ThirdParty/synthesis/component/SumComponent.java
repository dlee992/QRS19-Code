package ThirdParty.synthesis.component;

import ThirdParty.synthesis.basic.IOType;
import ThirdParty.synthesis.basic.Type;
import ThirdParty.synthesis.basic.VarType;

import com.microsoft.z3.ArithExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Z3Exception;

public final class SumComponent extends NumComponent {

	public SumComponent(int inputNum) {
		this(inputNum, VarType.INTEGER);
	}

	public SumComponent(int inputNum, VarType vt) {
		this.inputNum = inputNum;
		type = Components.SUM;
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

		spec = ctx.mkEq(sum, res);
	}

	public String getProg(String[] paras) {
		StringBuffer sb = new StringBuffer();
		sb.append("SUM(");
		for (int i = 0; i < paras.length - 1; i++) {
			sb.append(paras[i] + ",");
		}
		sb.append(paras[paras.length - 1]);
		sb.append(")");
		return sb.toString();
	}

	public boolean equals(Object obj) {
		if (obj instanceof SumComponent) {
			if (((SumComponent) obj).getInputNum() == this.inputNum) {
				return true;
			}
		}
		return false;
	}

}
