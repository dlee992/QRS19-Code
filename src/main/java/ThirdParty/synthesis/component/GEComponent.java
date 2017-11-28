package ThirdParty.synthesis.component;

import ThirdParty.synthesis.basic.IOType;
import ThirdParty.synthesis.basic.Type;
import ThirdParty.synthesis.basic.VarType;

import com.microsoft.z3.ArithExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Z3Exception;

public final class GEComponent extends Component {

	public GEComponent() {
		this(VarType.INTEGER);
	}

	public GEComponent(VarType vt) {
		type = Components.GE;
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

		spec = ctx.mkEq(ctx.mkGe(input1, input2), res);
	}

	public String getProg(String[] paras) {
		return paras[0] + " >= " + paras[1];
	}

	public boolean equals(Object obj) {
		if (obj instanceof GEComponent) {
			return true;
		}
		return false;
	}
}
