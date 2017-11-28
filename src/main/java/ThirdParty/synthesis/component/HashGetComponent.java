package ThirdParty.synthesis.component;

import ThirdParty.synthesis.basic.IOType;
import ThirdParty.synthesis.basic.Type;
import ThirdParty.synthesis.basic.VarType;
import ThirdParty.synthesis.util.StringUtil;

import com.microsoft.z3.ArrayExpr;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.IntExpr;
import com.microsoft.z3.Z3Exception;

public final class HashGetComponent extends Component {

	public HashGetComponent() {
		type = Components.HASHGET;
		compId++;

		Type hashType = new Type(VarType.HASH, IOType.COMP_INPUT);
		Type keyType = new Type(VarType.INTEGER, IOType.COMP_INPUT);
		Type valueType = new Type(VarType.ARRAY, IOType.COMP_OUTPUT);

		varTypes.add(hashType);
		varTypes.add(keyType);
		varTypes.add(valueType);
	}

	public void init(Context ctx) throws Z3Exception {
		super.init(ctx);

		ArrayExpr hash = (ArrayExpr) variables.get(0);
		IntExpr key = (IntExpr) variables.get(1);
		ArrayExpr value = (ArrayExpr) variables.get(2);

		BoolExpr equal = ctx.mkEq(ctx.mkSelect(hash, key), value);

		// extra key constraint.
		BoolExpr keyCons = ctx.mkAnd(ctx.mkGe(key, ctx.mkInt(0)),
				ctx.mkLt(key, ctx.mkInt(StringUtil.maxLen)));

		spec = ctx.mkAnd(keyCons, equal);
	}

	public String getProg(String[] paras) {
		return "hash.get(" + paras[0] + ", " + paras[1] + ")";
	}

	public boolean equals(Object obj) {
		if (obj instanceof HashGetComponent) {
			return true;
		}
		return false;
	}
}
