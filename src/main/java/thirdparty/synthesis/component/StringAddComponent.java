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

public final class StringAddComponent extends Component {

	public StringAddComponent() {
		type = Components.STRINGADD;
		compId++;

		Type inArrayType = new Type(VarType.ARRAY, IOType.COMP_INPUT);
		Type indexType = new Type(VarType.INTEGER, IOType.COMP_INPUT);
		Type valType = new Type(VarType.INTEGER, IOType.COMP_INPUT);

		Type outArrayType = new Type(VarType.ARRAY, IOType.COMP_OUTPUT);

		varTypes.add(inArrayType);
		varTypes.add(indexType);
		varTypes.add(valType);
		varTypes.add(outArrayType);
	}

	public void init(Context ctx) throws Z3Exception {
		super.init(ctx);

		ArrayExpr inArr = (ArrayExpr) variables.get(0);
		IntExpr index = (IntExpr) variables.get(1);
		IntExpr val = (IntExpr) variables.get(2);
		ArrayExpr outArr = (ArrayExpr) variables.get(3);

		IntExpr inLen = ctx.mkIntConst(Z3Util.getVarName());
		IntExpr outLen = ctx.mkIntConst(Z3Util.getVarName());

		BoolExpr inLenCons = StringUtil.lengthConstraint(ctx, inArr, inLen);
		BoolExpr outLenCons = StringUtil.lengthConstraint(ctx, outArr, outLen);
		BoolExpr lenCons = ctx.mkEq(ctx.mkAdd(inLen, ctx.mkInt(1)), outLen);
		BoolExpr paraCons = ctx.mkAnd(ctx.mkGe(index, ctx.mkInt(0)),
				ctx.mkLe(index, inLen));

		BoolExpr eleEq = StringUtil.addConstraintBound(ctx, inArr, inLen,
				outArr, outLen, index, val);

		spec = ctx.mkAnd(inLenCons, outLenCons, lenCons, paraCons, eleEq,
				StringUtil.wellFormedStringBound(ctx, outArr));
	}

	public String getProg(String[] paras) {
		return "add(" + paras[0] + ", " + paras[1] + ", " + paras[2] + ")";
	}

	public boolean equals(Object obj) {
		if (obj instanceof StringAddComponent) {
			return true;
		}
		return false;
	}
}
