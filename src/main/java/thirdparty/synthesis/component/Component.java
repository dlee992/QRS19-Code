package thirdparty.synthesis.component;

import java.util.ArrayList;
import java.util.List;

import thirdparty.synthesis.basic.Type;
import thirdparty.synthesis.basic.VarType;
import thirdparty.synthesis.util.Z3Util;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Z3Exception;

public abstract class Component {

	static int compId = 0;

	public int type;
	
	public boolean switchable = false;
	
	public VarType varType = null;

	List<Type> varTypes = new ArrayList<Type>();

	List<Expr> variables = new ArrayList<Expr>();

	BoolExpr spec;

	String prog;

	public void init(Context ctx) throws Z3Exception {
		for (int i = 0; i < varTypes.size(); i++) {
			String varName = "var_" + compId + "_" + i;
			variables.add(Z3Util.getVariable(varTypes.get(i).varType, ctx,
					varName));
		}
	}

	public int getType() {
		return type;
	}

	public List<Type> getVarTypes() {
		return varTypes;
	}

	public List<Expr> getVariables() {
		return variables;
	}

	public BoolExpr getSpecification() {
		return spec;
	}

	public abstract String getProg(String[] paras);

	public String toString() {
		return this.getClass().toString().substring(26);
	}
}
