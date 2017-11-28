package ThirdParty.synthesis.component;

import ThirdParty.synthesis.basic.IOType;
import ThirdParty.synthesis.basic.Type;
import ThirdParty.synthesis.basic.VarType;
import ThirdParty.synthesis.util.StringUtil;

import com.microsoft.z3.ArrayExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.IntExpr;
import com.microsoft.z3.Z3Exception;

public final class StringLengthComponent extends Component {

	public StringLengthComponent() {
		type = Components.STRINGLENGTH;
		compId++;

		Type arrayType = new Type(VarType.ARRAY, IOType.COMP_INPUT);
		Type lengthType = new Type(VarType.INTEGER, IOType.COMP_OUTPUT);

		varTypes.add(arrayType);
		varTypes.add(lengthType);
	}

	public void init(Context ctx) throws Z3Exception {
		super.init(ctx);

		spec = StringUtil.lengthConstraint(ctx, (ArrayExpr) variables.get(0),
				(IntExpr) variables.get(1));
	}

	public String getProg(String[] paras) {
		return "length(" + paras[0] + ")";
	}

	public boolean equals(Object obj) {
		if (obj instanceof StringLengthComponent) {
			return true;
		}
		return false;
	}
}
