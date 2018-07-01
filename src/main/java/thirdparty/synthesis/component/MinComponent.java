package thirdparty.synthesis.component;

import thirdparty.synthesis.basic.IOType;
import thirdparty.synthesis.basic.Type;
import thirdparty.synthesis.basic.VarType;

import com.microsoft.z3.ArithExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Z3Exception;

public final class MinComponent extends NumComponent {

	public MinComponent(int inputNum) {
		this(inputNum, VarType.INTEGER);
	}

	public MinComponent(int inputNum, VarType vt) {
		this.inputNum = inputNum;
		type = Components.MIN;
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

		ArithExpr min = (ArithExpr) variables.get(0);
		for (int i = 1; i < inputNum; i++) {
			ArithExpr value = (ArithExpr) variables.get(i);
			min = (ArithExpr) ctx.mkITE(ctx.mkLt(value, min), value, min);
		}

		spec = ctx.mkEq(min, res);
	}

	public String getProg(String[] paras) {
		StringBuffer sb = new StringBuffer();
		sb.append("MIN(");
		for (int i = 0; i < paras.length - 1; i++) {
			sb.append(paras[i] + ",");
		}
		sb.append(paras[paras.length - 1]);
		sb.append(")");
		return sb.toString();
	}

	public boolean equals(Object obj) {
		if (obj instanceof MinComponent) {
			if (((MinComponent) obj).getInputNum() == this.inputNum) {
				return true;
			}
		}
		return false;
	}

}
