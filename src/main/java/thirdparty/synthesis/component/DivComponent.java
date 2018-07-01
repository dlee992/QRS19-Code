package thirdparty.synthesis.component;

import thirdparty.synthesis.basic.IOType;
import thirdparty.synthesis.basic.Type;
import thirdparty.synthesis.basic.VarType;

import com.microsoft.z3.ArithExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Z3Exception;

public final class DivComponent extends Component {

	public DivComponent() {
		this(VarType.INTEGER);
	}

	public DivComponent(VarType vt) {
		type = Components.DIV;
		varType = vt;
		compId++;
		switchable = false;

		Type inType = new Type(vt, IOType.COMP_INPUT);
		Type outType = new Type(vt, IOType.COMP_OUTPUT);
		varTypes.add(inType);
		varTypes.add(inType);
		varTypes.add(outType);
	}

	public void init(Context ctx) throws Z3Exception {
		super.init(ctx);

		ArithExpr input1 = (ArithExpr) variables.get(0);
		ArithExpr input2 = (ArithExpr) variables.get(1);
		Expr res = variables.get(2);

		Expr div = null;
		if (varType == VarType.INTEGER) {
			// For integer, the solver could return values. This is integer division!
			div = ctx.mkITE(ctx.mkEq(input2, ctx.mkInt(0)), ctx.mkInt(0),
					ctx.mkDiv(input1, input2));
		}
		if (varType == VarType.DOUBLE) {
			// For double, the solver would return unknown. It's bad!!!
			div = ctx.mkITE(ctx.mkEq(input2, ctx.mkReal(0)), ctx.mkReal(0),
					ctx.mkDiv(input1, input2));
		}

		spec = ctx.mkEq(div, res);
	}

	public String getProg(String[] paras) {
		return paras[0] + " / " + paras[1];
	}

	public boolean equals(Object obj) {
		if (obj instanceof DivComponent) {
			return true;
		}
		return false;
	}

}
