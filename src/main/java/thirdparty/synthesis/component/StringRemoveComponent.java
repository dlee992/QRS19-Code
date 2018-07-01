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

public final class StringRemoveComponent extends Component {

	public StringRemoveComponent() {
		type = Components.STRINGREMOVE;
		compId++;

		Type inArrayType = new Type(VarType.ARRAY, IOType.COMP_INPUT);
		Type iType = new Type(VarType.INTEGER, IOType.COMP_INPUT);

		Type outArrayType = new Type(VarType.ARRAY, IOType.COMP_OUTPUT);

		varTypes.add(inArrayType);
		varTypes.add(iType);
		varTypes.add(outArrayType);
	}

	public void init(Context ctx) throws Z3Exception {
		super.init(ctx);

		ArrayExpr inArr = (ArrayExpr) variables.get(0);
		IntExpr index = (IntExpr) variables.get(1);
		ArrayExpr outArr = (ArrayExpr) variables.get(2);

		IntExpr inLen = ctx.mkIntConst(Z3Util.getVarName());
		IntExpr outLen = ctx.mkIntConst(Z3Util.getVarName());
		BoolExpr paraCons = ctx.mkAnd(ctx.mkGe(index, ctx.mkInt(0)),
				ctx.mkLt(index, inLen));

		BoolExpr inLenCons = StringUtil.lengthConstraint(ctx, inArr, inLen);
		BoolExpr outLenCons = StringUtil.lengthConstraint(ctx, outArr, outLen);
		BoolExpr lenCons = ctx.mkEq(ctx.mkAdd(outLen, ctx.mkInt(1)), inLen);

		BoolExpr eleEq = StringUtil.removeConstraintBound(ctx, inArr, inLen,
				outArr, outLen, index);

		spec = ctx.mkAnd(paraCons, inLenCons, outLenCons, lenCons, eleEq,
				StringUtil.wellFormedStringBound(ctx, outArr));
	}

	public String getProg(String[] paras) {
		return "remove(" + paras[0] + ", " + paras[1] + ")";
	}

	public boolean equals(Object obj) {
		if (obj instanceof StringRemoveComponent) {
			return true;
		}
		return false;
	}
}
