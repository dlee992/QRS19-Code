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

public final class StringAssignComponent extends Component {

	public StringAssignComponent() {
		type = Components.STRINGASSIGN;
		compId++;

		// string_a(start) = string_b
		Type arrayAType = new Type(VarType.ARRAY, IOType.COMP_INPUT);
		Type startType = new Type(VarType.INTEGER, IOType.COMP_INPUT);
		Type arrayBType = new Type(VarType.ARRAY, IOType.COMP_INPUT);

		Type retArrayType = new Type(VarType.ARRAY, IOType.COMP_OUTPUT);

		varTypes.add(arrayAType);
		varTypes.add(startType);
		varTypes.add(arrayBType);
		varTypes.add(retArrayType);
	}

	public void init(Context ctx) throws Z3Exception {
		super.init(ctx);

		ArrayExpr arrA = (ArrayExpr) variables.get(0);
		IntExpr start = (IntExpr) variables.get(1);
		ArrayExpr arrB = (ArrayExpr) variables.get(2);
		ArrayExpr arrRet = (ArrayExpr) variables.get(3);

		IntExpr lenA = ctx.mkIntConst(Z3Util.getVarName());
		BoolExpr lenACons = StringUtil.lengthConstraint(ctx, arrA, lenA);

		IntExpr lenB = ctx.mkIntConst(Z3Util.getVarName());
		BoolExpr lenBCons = StringUtil.lengthConstraint(ctx, arrB, lenB);

		IntExpr lenRet = ctx.mkIntConst(Z3Util.getVarName());
		BoolExpr lenRetCons = ctx.mkEq(
				ctx.mkITE(ctx.mkGe(lenA, ctx.mkAdd(start, lenB)), lenA,
						ctx.mkAdd(start, lenB)), lenRet);

		BoolExpr startCons = ctx.mkAnd(ctx.mkGe(start, ctx.mkInt(0)),
				ctx.mkLe(start, lenA));

		BoolExpr assignCons = StringUtil.stringAssignConstraintBound(ctx, arrA,
				lenA, start, arrB, lenB, arrRet);

		BoolExpr fill0 = StringUtil.fillArray0Bound(ctx, arrRet, lenRet);

		spec = ctx.mkAnd(lenACons, lenBCons, lenRetCons, startCons, assignCons,
				fill0);
	}

	public String getProg(String[] paras) {
		return "assign(" + paras[0] + ", " + paras[1] + ", " + paras[2] + ")";
	}

	public boolean equals(Object obj) {
		if (obj instanceof StringAssignComponent) {
			return true;
		}
		return false;
	}
}
