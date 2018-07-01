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

public final class StringN21Component extends Component {

	public StringN21Component() {
		type = Components.STRINGN21;
		compId++;

		Type inArrayType = new Type(VarType.ARRAY, IOType.COMP_INPUT);
		Type outArrayType = new Type(VarType.INTEGER, IOType.COMP_OUTPUT);

		varTypes.add(inArrayType);
		varTypes.add(outArrayType);
	}

	public void init(Context ctx) throws Z3Exception {
		super.init(ctx);

		ArrayExpr arrInput = (ArrayExpr) variables.get(0);
		IntExpr ret = (IntExpr) variables.get(1);

		IntExpr lenInput = ctx.mkIntConst(Z3Util.getVarName());
		IntExpr lenRet = ctx.mkIntConst(Z3Util.getVarName());

		BoolExpr len1Cons = StringUtil
				.lengthConstraint(ctx, arrInput, lenInput);
		BoolExpr lenCons = ctx.mkEq(lenInput, lenRet);

		BoolExpr sumEqual = StringUtil.N21ConstraintBound(ctx, arrInput,
				lenInput, ret);

		spec = ctx.mkAnd(len1Cons, lenCons, sumEqual);
	}

	public String getProg(String[] paras) {
		return "N21(" + paras[0] + ")";
	}

	public boolean equals(Object obj) {
		if (obj instanceof StringN21Component) {
			return true;
		}
		return false;
	}
}
