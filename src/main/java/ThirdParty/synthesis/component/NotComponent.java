package ThirdParty.synthesis.component;

import ThirdParty.synthesis.basic.IOType;
import ThirdParty.synthesis.basic.Type;
import ThirdParty.synthesis.basic.VarType;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Z3Exception;

public final class NotComponent extends Component {

	public NotComponent() {
		type = Components.NOT;
		compId++;

		Type inType = new Type(VarType.BOOLEAN, IOType.COMP_INPUT);
		Type outType = new Type(VarType.BOOLEAN, IOType.COMP_OUTPUT);
		varTypes.add(inType);
		varTypes.add(outType);
	}

	public void init(Context ctx) throws Z3Exception {
		super.init(ctx);

		BoolExpr input = (BoolExpr) variables.get(0);
		Expr res = variables.get(1);

		spec = ctx.mkEq(ctx.mkNot(input), res);
	}

	public String getProg(String[] paras) {
		return " !" + paras[0];
	}

	public boolean equals(Object obj) {
		if (obj instanceof NotComponent) {
			return true;
		}
		return false;
	}
}
