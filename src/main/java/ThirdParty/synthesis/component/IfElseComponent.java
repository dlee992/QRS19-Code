package ThirdParty.synthesis.component;

import ThirdParty.synthesis.basic.IOType;
import ThirdParty.synthesis.basic.Type;
import ThirdParty.synthesis.basic.VarType;

import com.microsoft.z3.ArithExpr;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Z3Exception;

public final class IfElseComponent extends Component {

	public IfElseComponent() {
		this(VarType.INTEGER);
	}

	public IfElseComponent(VarType vt) {
		type = Components.IFELSE;
		varType = vt;
		compId++;

		Type boolType = new Type(VarType.BOOLEAN, IOType.COMP_INPUT);
		Type inType = new Type(vt, IOType.COMP_INPUT);
		Type outType = new Type(vt, IOType.COMP_OUTPUT);
		varTypes.add(boolType);
		varTypes.add(inType);
		varTypes.add(inType);
		varTypes.add(outType);
	}

	public void init(Context ctx) throws Z3Exception {
		super.init(ctx);

		BoolExpr cond = (BoolExpr) variables.get(0);
		ArithExpr input1 = (ArithExpr) variables.get(1);
		ArithExpr input2 = (ArithExpr) variables.get(2);
		Expr res = variables.get(3);

		spec = ctx.mkEq(ctx.mkITE(cond, input1, input2), res);
	}

	public String getProg(String[] paras) {
		return paras[0] + " ? " + paras[1] + " : " + paras[2];
	}

	public boolean equals(Object obj) {
		if (obj instanceof IfElseComponent) {
			return true;
		}
		return false;
	}
}
