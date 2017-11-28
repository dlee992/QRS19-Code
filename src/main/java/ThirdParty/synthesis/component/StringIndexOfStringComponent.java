package ThirdParty.synthesis.component;

import ThirdParty.synthesis.basic.IOType;
import ThirdParty.synthesis.basic.Type;
import ThirdParty.synthesis.basic.VarType;
import ThirdParty.synthesis.util.StringUtil;
import ThirdParty.synthesis.util.Z3Util;

import com.microsoft.z3.ArrayExpr;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.IntExpr;
import com.microsoft.z3.Z3Exception;

public final class StringIndexOfStringComponent extends Component {

	public StringIndexOfStringComponent() {
		type = Components.STRINGINDEXOFSTRING;
		compId++;

		Type inArrayType = new Type(VarType.ARRAY, IOType.COMP_INPUT);
		Type outType = new Type(VarType.INTEGER, IOType.COMP_OUTPUT);

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

		IntExpr iMin = ctx.mkIntConst(Z3Util.getVarName());

		BoolExpr iMinCons = StringUtil.indexOfConstraintBound(ctx, arr1, len1,
				arr2, len2, iMin);

		// when contain
		BoolExpr contain = StringUtil.containConstraintBound(ctx, arr1, len1,
				arr2, len2);
		contain = ctx.mkAnd(ctx.mkGt(len1, ctx.mkInt(0)),
				ctx.mkGt(len2, ctx.mkInt(0)), contain);

		BoolExpr resCons = (BoolExpr) ctx.mkITE(contain,
				ctx.mkAnd(iMinCons, ctx.mkEq(ret, iMin)),
				ctx.mkEq(ret, ctx.mkInt(-1)));

		spec = ctx.mkAnd(len1Cons, len2Cons, resCons);
	}

	public String getProg(String[] paras) {
		return "indexof(" + paras[0] + ", " + paras[1] + ")";
	}

	public boolean equals(Object obj) {
		if (obj instanceof StringIndexOfStringComponent) {
			return true;
		}
		return false;
	}
}
