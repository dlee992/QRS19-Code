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

public final class StringIndexOfCharComponent extends Component {

	public StringIndexOfCharComponent() {
		type = Components.STRINGINDEXOFCHAR;
		compId++;

		Type inArrayType = new Type(VarType.ARRAY, IOType.COMP_INPUT);
		Type valType = new Type(VarType.INTEGER, IOType.COMP_INPUT);
		Type outType = new Type(VarType.INTEGER, IOType.COMP_OUTPUT);

		varTypes.add(inArrayType);
		varTypes.add(valType);
		varTypes.add(outType);
	}

	public void init(Context ctx) throws Z3Exception {
		super.init(ctx);

		IntExpr len1 = ctx.mkIntConst(Z3Util.getVarName());
		IntExpr len2 = ctx.mkIntConst(Z3Util.getVarName());

		ArrayExpr arr1 = (ArrayExpr) variables.get(0);
		IntExpr val = (IntExpr) variables.get(1);
		Expr ret = variables.get(2);

		BoolExpr len1Cons = StringUtil.lengthConstraint(ctx, arr1, len1);

		ArrayExpr arr2 = (ArrayExpr) Z3Util.getVariable(VarType.ARRAY, ctx,
				Z3Util.getVarName());
		BoolExpr len2Cons = StringUtil.lengthConstraint(ctx, arr2, len2);
		BoolExpr len2Cons2 = ctx.mkEq(len2, ctx.mkInt(1));
		BoolExpr firstEle = ctx.mkEq(ctx.mkSelect(arr2, ctx.mkInt(0)), val);
		BoolExpr valCons = ctx.mkAnd(len2Cons, len2Cons2, firstEle);

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

		spec = ctx.mkAnd(len1Cons, valCons, resCons);
	}

	public String getProg(String[] paras) {
		return "indexof(" + paras[0] + ", " + paras[1] + ")";
	}

	public boolean equals(Object obj) {
		if (obj instanceof StringIndexOfCharComponent) {
			return true;
		}
		return false;
	}
}
