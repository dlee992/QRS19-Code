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

public final class SubStringComponent extends Component {

	public SubStringComponent() {
		type = Components.SUBSTRING;
		compId++;

		Type inArrayType = new Type(VarType.ARRAY, IOType.COMP_INPUT);

		Type iType = new Type(VarType.INTEGER, IOType.COMP_INPUT);
		Type jType = new Type(VarType.INTEGER, IOType.COMP_INPUT);

		Type retArrayType = new Type(VarType.ARRAY, IOType.COMP_OUTPUT);

		varTypes.add(inArrayType);
		varTypes.add(iType);
		varTypes.add(jType);
		varTypes.add(retArrayType);
	}

	public void init(Context ctx) throws Z3Exception {
		super.init(ctx);

		ArrayExpr arr = (ArrayExpr) variables.get(0);
		IntExpr i = (IntExpr) variables.get(1);
		IntExpr j = (IntExpr) variables.get(2);
		ArrayExpr retArr = (ArrayExpr) variables.get(3);

		IntExpr len = ctx.mkIntConst(Z3Util.getVarName());
		BoolExpr lenCons = StringUtil.lengthConstraint(ctx, arr, len);

		BoolExpr paraCons = ctx.mkAnd(lenCons, ctx.mkGe(i, ctx.mkInt(0)),
				ctx.mkLe(j, len));

		BoolExpr allEleEqual = StringUtil.substringConstraintBound(ctx, arr, i,
				j, retArr);

		// change other elements to 0
		BoolExpr allEleEqual0 = StringUtil.fillArray0Bound(ctx, retArr,
				ctx.mkAdd(j, ctx.mkUnaryMinus(i)));

		BoolExpr rightParaCons = ctx.mkAnd(ctx.mkLe(i, j), paraCons,
				allEleEqual, allEleEqual0);
		BoolExpr wrongParaCons = ctx.mkAnd(ctx.mkNot(ctx.mkLe(i, j)), paraCons,
				allEleEqual0);

		spec = ctx.mkOr(rightParaCons, wrongParaCons);
	}

	public String getProg(String[] paras) {
		return "substring(" + paras[0] + ", " + paras[1] + ", " + paras[2]
				+ ")";
	}

	public boolean equals(Object obj) {
		if (obj instanceof SubStringComponent) {
			return true;
		}
		return false;
	}
}
