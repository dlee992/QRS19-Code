package thirdparty.synthesis.component;

import thirdparty.synthesis.basic.IOType;
import thirdparty.synthesis.basic.Type;
import thirdparty.synthesis.basic.VarType;
import thirdparty.synthesis.util.StringUtil;

import com.microsoft.z3.ArrayExpr;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.IntExpr;
import com.microsoft.z3.Z3Exception;

public final class ConstantComponent extends Component {

	public Object defaultValue = null;

	public ConstantComponent() {
		this(VarType.INTEGER, null);
	}

	public ConstantComponent(Object dv) {
		this(VarType.INTEGER, dv);
	}

	public ConstantComponent(VarType vt) {
		this(vt, null);
	}

	public ConstantComponent(VarType vt, Object dv) {
		type = Components.CONSTANT;
		varType = vt;
		defaultValue = dv;
		compId++;

		Type type = new Type(vt, IOType.COMP_OUTPUT);
		varTypes.add(type);
	}

	public void init(Context ctx) throws Z3Exception {
		super.init(ctx);

		Expr res = variables.get(0);

		if (varType == VarType.ARRAY) {
			spec = StringUtil.wellFormedStringBound(ctx, (ArrayExpr) res);
			return;
		}

		if (varType == VarType.HASH) {
			spec = ctx.mkBool(true);
			for (int i = 0; i < StringUtil.maxLen; i++) {
				IntExpr key = ctx.mkInt(i);
				ArrayExpr value = (ArrayExpr) ctx
						.mkSelect((ArrayExpr) res, key);
				BoolExpr valueCons = StringUtil.wellFormedStringBound(ctx,
						value);

				// extra, set the values as empty string.
				BoolExpr extraValueCons = StringUtil.fillArray0Bound(ctx,
						value, 0);
				spec = ctx.mkAnd(spec, valueCons, extraValueCons);
			}
			return;
		}

		// support default values.
		if (defaultValue != null) {
			if (varType == VarType.INTEGER) {
				spec = ctx.mkEq(res, ctx.mkInt((Integer) defaultValue));
			}
			if (varType == VarType.DOUBLE) {
				spec = ctx.mkEq(res, ctx.mkReal(defaultValue.toString()));
			}
			if (varType == VarType.BOOLEAN) {
				spec = ctx.mkEq(res, ctx.mkBool((Boolean) defaultValue));
			}
		} else {
			spec = ctx.mkBool(true);
		}
	}

	public String getProg(String[] paras) {
		return paras[0];
	}

}
