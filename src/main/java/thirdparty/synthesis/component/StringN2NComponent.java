package thirdparty.synthesis.component;

import thirdparty.synthesis.basic.IOType;
import thirdparty.synthesis.basic.Type;
import thirdparty.synthesis.basic.VarType;
import thirdparty.synthesis.util.StringUtil;
import thirdparty.synthesis.util.Z3Util;

import com.microsoft.z3.ArrayExpr;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.IntExpr;
import com.microsoft.z3.Z3Exception;

public final class StringN2NComponent extends Component {

	public StringN2NComponent() {
		type = Components.STRINGN2N;
		compId++;

		Type inArrayType = new Type(VarType.ARRAY, IOType.COMP_INPUT);
		Type outArrayType = new Type(VarType.ARRAY, IOType.COMP_OUTPUT);

		varTypes.add(inArrayType);
		varTypes.add(outArrayType);
	}

	public void init(Context ctx) throws Z3Exception {
		super.init(ctx);

		ArrayExpr arrInput = (ArrayExpr) variables.get(0);
		ArrayExpr arrRet = (ArrayExpr) variables.get(1);

		IntExpr lenInput = ctx.mkIntConst(Z3Util.getVarName());
		IntExpr lenRet = ctx.mkIntConst(Z3Util.getVarName());

		BoolExpr len1Cons = StringUtil
				.lengthConstraint(ctx, arrInput, lenInput);
		BoolExpr lenCons = ctx.mkEq(lenInput, lenRet);

		BoolExpr allEleEqual = StringUtil.N2NConstraintBound(ctx, arrInput,
				lenInput, arrRet);

		// change other elements to 0
		BoolExpr allEleEqual0 = StringUtil.fillArray0Bound(ctx,
				(ArrayExpr) arrRet, lenRet);

		spec = ctx.mkAnd(len1Cons, lenCons, allEleEqual, allEleEqual0);
	}

	public String getProg(String[] paras) {
		return "N2N(" + paras[0] + ")";
	}

	public boolean equals(Object obj) {
		if (obj instanceof StringN2NComponent) {
			return true;
		}
		return false;
	}
}
