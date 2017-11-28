package ThirdParty.synthesis.basic;

import java.util.ArrayList;
import java.util.List;

import ThirdParty.synthesis.component.Component;
import ThirdParty.synthesis.component.Components;
import ThirdParty.synthesis.util.Z3Util;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.IntExpr;
import com.microsoft.z3.Z3Exception;

public class ProgramAbstract {

	private static int totalId = 1;

	protected int id = totalId++;

	public List<Component> components = null;
	public List<Type> inputTypes = null;
	public Type outputType = null;
	public List<Type> types = new ArrayList<Type>();
	public List<IntExpr> locVars = new ArrayList<IntExpr>();
	public List<Expr> constVars = new ArrayList<Expr>();

	public ProgramAbstract(List<Component> components, List<Type> inputTypes,
			Type outputType) {

		// initialize types
		for (Type type : inputTypes) {
			type.ioType = IOType.FUN_INPUT;
		}
		if (outputType != null) {
			outputType.ioType = IOType.FUN_OUTPUT;
		}

		this.components = components;
		this.inputTypes = inputTypes;
		this.outputType = outputType;
	}

	public ProgramInstance getInstance() {
		return new ProgramInstance(this);
	}

	public boolean isReturnVoid() {
		return outputType == null;
	}

	public int getInputSize() {
		return inputTypes.size();
	}

	public int getProgramSize() {
		return getInputSize() + components.size();
	}

	public void init(Context ctx) throws Z3Exception {
		// sort the components, make the constants in the first, and init the
		// components
		List<Component> newComps = Components.getComponents(components, ctx);
		components.clear();

		for (Component comp : newComps) {
			if (comp.getType() == Components.CONSTANT) {
				components.add(comp);
			}
		}
		for (Component comp : newComps) {
			if (comp.getType() != Components.CONSTANT) {
				components.add(comp);
			}
		}

		// init the types
		types = extractTypes();

		// init the loc variables
		for (int i = 0; i < types.size(); i++) {
			IntExpr locVar = ctx.mkIntConst("loc_" + id + "_" + i);
			locVars.add(locVar);
		}

		// init the constant variables
		int constIndex = 0;
		for (Component comp : components) {
			if (comp.getType() == Components.CONSTANT) {
				Expr constVar = Z3Util.getVariable(comp.varType, ctx, "const_"
						+ id + "-" + constIndex);
				constVars.add(constVar);
				constIndex++;
			}
		}
	}

	public List<Type> extractTypes() {

		for (Type type : inputTypes) {
			type.ioType = IOType.FUN_INPUT;
			types.add(type);
		}

		// all the components
		for (Component comp : components) {
			types.addAll(comp.getVarTypes());
		}

		// function output
		if (!isReturnVoid()) {
			types.add(outputType);
		}

		return types;
	}

}
