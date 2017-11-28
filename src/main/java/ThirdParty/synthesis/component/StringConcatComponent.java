package ThirdParty.synthesis.component;

import ThirdParty.synthesis.basic.IOType;
import ThirdParty.synthesis.basic.Type;
import ThirdParty.synthesis.basic.VarType;
import ThirdParty.synthesis.util.StringUtil;
import ThirdParty.synthesis.util.Z3Util;

import com.microsoft.z3.ArrayExpr;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.IntExpr;
import com.microsoft.z3.Z3Exception;

public final class StringConcatComponent extends Component {

	public StringConcatComponent() {
		type = Components.STRINGCONCAT;
		compId++;

		Type inArrayType = new Type(VarType.ARRAY, IOType.COMP_INPUT);
		Type outArrayType = new Type(VarType.ARRAY, IOType.COMP_OUTPUT);

		varTypes.add(inArrayType);
		varTypes.add(inArrayType);
		varTypes.add(outArrayType);
	}

	public void init(Context ctx) throws Z3Exception {
		super.init(ctx);

		ArrayExpr arr1 = (ArrayExpr) variables.get(0);
		ArrayExpr arr2 = (ArrayExpr) variables.get(1);
		ArrayExpr arr = (ArrayExpr) variables.get(2);

		IntExpr len1 = ctx.mkIntConst(Z3Util.getVarName());
		IntExpr len2 = ctx.mkIntConst(Z3Util.getVarName());
		IntExpr len = ctx.mkIntConst(Z3Util.getVarName());

		BoolExpr len1Cons = StringUtil.lengthConstraint(ctx, arr1, len1);
		BoolExpr len2Cons = StringUtil.lengthConstraint(ctx, arr2, len2);
		BoolExpr lenCons = ctx.mkEq(ctx.mkAdd(len1, len2), len);

		BoolExpr allEleEqual = StringUtil.concatConstraintBound(ctx, arr1,
				len1, arr2, len2, arr, len);

		// change other elements to 0
		BoolExpr allEleEqual0 = StringUtil.fillArray0Bound(ctx,
				(ArrayExpr) arr, ctx.mkAdd(len1, len2));

		spec = ctx
				.mkAnd(len1Cons, len2Cons, lenCons, allEleEqual, allEleEqual0);
	}

	public String getProg(String[] paras) {
		return "concat(" + paras[0] + ", " + paras[1] + ")";
	}

	public boolean equals(Object obj) {
		if (obj instanceof StringConcatComponent) {
			return true;
		}
		return false;
	}
}
