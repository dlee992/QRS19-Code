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

public final class StringIndexAtComponent extends Component {

	public StringIndexAtComponent() {
		type = Components.STRINGINDEXAT;
		compId++;

		Type inArrayType = new Type(VarType.ARRAY, IOType.COMP_INPUT);
		Type iType = new Type(VarType.INTEGER, IOType.COMP_INPUT);

		Type valueType = new Type(VarType.INTEGER, IOType.COMP_OUTPUT);

		varTypes.add(inArrayType);
		varTypes.add(iType);
		varTypes.add(valueType);
	}

	public void init(Context ctx) throws Z3Exception {
		super.init(ctx);

		ArrayExpr arr = (ArrayExpr) variables.get(0);
		IntExpr i = (IntExpr) variables.get(1);
		IntExpr value = (IntExpr) variables.get(2);
		IntExpr len = ctx.mkIntConst(Z3Util.getVarName());

		BoolExpr lenCons = StringUtil.lengthConstraint(ctx, arr, len);
		BoolExpr paraCons = ctx.mkAnd(ctx.mkGe(i, ctx.mkInt(0)),
				ctx.mkLt(i, len));

		BoolExpr equal = ctx.mkEq(ctx.mkSelect(arr, i), value);

		spec = ctx.mkAnd(lenCons, paraCons, equal);
	}

	public String getProg(String[] paras) {
		return "indexat(" + paras[0] + ", " + paras[1] + ")";
	}

	public boolean equals(Object obj) {
		if (obj instanceof StringIndexAtComponent) {
			return true;
		}
		return false;
	}
}
