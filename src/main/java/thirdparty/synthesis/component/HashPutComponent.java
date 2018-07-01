package thirdparty.synthesis.component;

import thirdparty.synthesis.basic.IOType;
import thirdparty.synthesis.basic.Type;
import thirdparty.synthesis.basic.VarType;
import thirdparty.synthesis.util.StringUtil;

import com.microsoft.z3.ArrayExpr;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.IntExpr;
import com.microsoft.z3.Z3Exception;

public final class HashPutComponent extends Component {

	public HashPutComponent() {
		type = Components.HASHPUT;
		compId++;

		Type hashType = new Type(VarType.HASH, IOType.COMP_INPUT);
		Type keyType = new Type(VarType.INTEGER, IOType.COMP_INPUT);
		Type valueType = new Type(VarType.ARRAY, IOType.COMP_INPUT);
		Type retHashType = new Type(VarType.HASH, IOType.COMP_OUTPUT);

		varTypes.add(hashType);
		varTypes.add(keyType);
		varTypes.add(valueType);
		varTypes.add(retHashType);
	}

	public void init(Context ctx) throws Z3Exception {
		super.init(ctx);

		ArrayExpr hash = (ArrayExpr) variables.get(0);
		IntExpr key = (IntExpr) variables.get(1);
		ArrayExpr value = (ArrayExpr) variables.get(2);
		ArrayExpr retHash = (ArrayExpr) variables.get(3);

		BoolExpr storeCons = ctx.mkEq(ctx.mkStore(hash, key, value), retHash);

		// extra key constraint.
		BoolExpr keyCons = ctx.mkAnd(ctx.mkGe(key, ctx.mkInt(0)),
				ctx.mkLt(key, ctx.mkInt(StringUtil.maxLen)));

		spec = ctx.mkAnd(keyCons, storeCons);
	}

	public String getProg(String[] paras) {
		return "hash.put(" + paras[0] + ", " + paras[1] + ", " + paras[2] + ")";
	}

	public boolean equals(Object obj) {
		if (obj instanceof HashPutComponent) {
			return true;
		}
		return false;
	}
}
