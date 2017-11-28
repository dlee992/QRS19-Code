package ThirdParty.synthesis.component;

import ThirdParty.synthesis.basic.IOType;
import ThirdParty.synthesis.basic.Type;
import ThirdParty.synthesis.basic.VarType;

import com.microsoft.z3.ArithExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Z3Exception;

public final class LtComponent extends Component {

	public LtComponent() {
		this(VarType.INTEGER);
	}

	public LtComponent(VarType vt) {
		type = Components.LT;
		varType = vt;
		compId++;

		Type inType = new Type(vt, IOType.COMP_INPUT);
		Type outType = new Type(VarType.BOOLEAN, IOType.COMP_OUTPUT);
		varTypes.add(inType);
		varTypes.add(inType);
		varTypes.add(outType);
	}

	public void init(Context ctx) throws Z3Exception {
		super.init(ctx);

		ArithExpr input1 = (ArithExpr) variables.get(0);
		ArithExpr input2 = (ArithExpr) variables.get(1);
		Expr res = variables.get(2);

		spec = ctx.mkEq(ctx.mkLt(input1, input2), res);
	}

	public String getProg(String[] paras) {
		return paras[0] + " < " + paras[1];
	}

	public boolean equals(Object obj) {
		if (obj instanceof LtComponent) {
			return true;
		}
		return false;
	}
}
