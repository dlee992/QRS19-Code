package ThirdParty.synthesis.component;

import ThirdParty.synthesis.basic.IOType;
import ThirdParty.synthesis.basic.Type;
import ThirdParty.synthesis.basic.VarType;

import com.microsoft.z3.ArithExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Z3Exception;

public final class AbsComponent extends Component {

	public AbsComponent() {
		this(VarType.INTEGER);
	}

	public AbsComponent(VarType vt) {
		type = Components.ABS;
		varType = vt;
		compId++;

		Type inType = new Type(vt, IOType.COMP_INPUT);
		Type outType = new Type(vt, IOType.COMP_OUTPUT);
		varTypes.add(inType);
		varTypes.add(outType);
	}

	public void init(Context ctx) throws Z3Exception {
		super.init(ctx);

		ArithExpr input = (ArithExpr) variables.get(0);
		Expr res = variables.get(1);

		spec = ctx.mkEq(
				ctx.mkITE(ctx.mkLt(input, ctx.mkInt(0)),
						ctx.mkUnaryMinus(input), input), res);
	}

	public String getProg(String[] paras) {
		return "abs(" + paras[0] + ")";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof AbsComponent) {
			return true;
		}
		return false;
	}
}
