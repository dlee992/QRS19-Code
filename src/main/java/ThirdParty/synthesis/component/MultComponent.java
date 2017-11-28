package ThirdParty.synthesis.component;

import ThirdParty.synthesis.basic.IOType;
import ThirdParty.synthesis.basic.Type;
import ThirdParty.synthesis.basic.VarType;

import com.microsoft.z3.ArithExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Z3Exception;

public final class MultComponent extends Component {

	public MultComponent() {
		this(VarType.INTEGER);
	}

	public MultComponent(VarType vt) {
		type = Components.MULT;
		varType = vt;
		compId++;
		switchable = true;

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

		spec = ctx.mkEq(ctx.mkMul(input1, input2), res);
	}

	public String getProg(String[] paras) {
		return paras[0] + " * " + paras[1];
	}

	public boolean equals(Object obj) {
		if (obj instanceof MultComponent) {
			return true;
		}
		return false;
	}

}
