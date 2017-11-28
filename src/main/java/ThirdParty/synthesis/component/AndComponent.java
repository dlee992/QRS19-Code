package ThirdParty.synthesis.component;

import ThirdParty.synthesis.basic.IOType;
import ThirdParty.synthesis.basic.Type;
import ThirdParty.synthesis.basic.VarType;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Z3Exception;

public final class AndComponent extends Component {

	public AndComponent() {
		type = Components.AND;
		compId++;
		switchable = true;

		Type inType = new Type(VarType.BOOLEAN, IOType.COMP_INPUT);
		Type outType = new Type(VarType.BOOLEAN, IOType.COMP_OUTPUT);
		varTypes.add(inType);
		varTypes.add(inType);
		varTypes.add(outType);
	}

	public void init(Context ctx) throws Z3Exception {
		super.init(ctx);

		BoolExpr input1 = (BoolExpr) variables.get(0);
		BoolExpr input2 = (BoolExpr) variables.get(1);
		Expr res = variables.get(2);

		spec = ctx.mkEq(ctx.mkAnd(input1, input2), res);
	}

	public String getProg(String[] paras) {
		return paras[0] + " && " + paras[1];
	}

	public boolean equals(Object obj) {
		if (obj instanceof AndComponent) {
			return true;
		}
		return false;
	}
}
