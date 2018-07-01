package thirdparty.synthesis.component;

import thirdparty.synthesis.basic.IOType;
import thirdparty.synthesis.basic.Type;
import thirdparty.synthesis.basic.VarType;
import thirdparty.synthesis.util.StringUtil;
import thirdparty.synthesis.util.Z3Util;

import com.microsoft.z3.ArrayExpr;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.IntExpr;
import com.microsoft.z3.Z3Exception;

public final class StringContainComponent extends Component {

	public StringContainComponent() {
		type = Components.STRINGCONTAIN;
		compId++;

		Type inArrayType = new Type(VarType.ARRAY, IOType.COMP_INPUT);
		Type outType = new Type(VarType.BOOLEAN, IOType.COMP_OUTPUT);

		varTypes.add(inArrayType);
		varTypes.add(inArrayType);
		varTypes.add(outType);
	}

	public void init(Context ctx) throws Z3Exception {
		super.init(ctx);

		IntExpr len1 = ctx.mkIntConst(Z3Util.getVarName());
		IntExpr len2 = ctx.mkIntConst(Z3Util.getVarName());

		ArrayExpr arr1 = (ArrayExpr) variables.get(0);
		ArrayExpr arr2 = (ArrayExpr) variables.get(1);
		Expr ret = variables.get(2);

		BoolExpr len1Cons = StringUtil.lengthConstraint(ctx, arr1, len1);
		BoolExpr len2Cons = StringUtil.lengthConstraint(ctx, arr2, len2);

		BoolExpr contain = StringUtil.containConstraintBound(ctx, arr1, len1,
				arr2, len2);

		BoolExpr len0 = ctx.mkOr(ctx.mkEq(len1, ctx.mkInt(0)),
				ctx.mkEq(len2, ctx.mkInt(0)));

		BoolExpr containCons = (BoolExpr) ctx.mkITE(len0,
				ctx.mkEq(ret, ctx.mkFalse()), ctx.mkEq(ret, contain));

		spec = ctx.mkAnd(len1Cons, len2Cons, containCons);
	}

	public String getProg(String[] paras) {
		return "contains(" + paras[0] + ", " + paras[1] + ")";
	}

	public boolean equals(Object obj) {
		if (obj instanceof StringContainComponent) {
			return true;
		}
		return false;
	}
}
