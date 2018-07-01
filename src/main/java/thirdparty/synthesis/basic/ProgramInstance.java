package thirdparty.synthesis.basic;

import java.util.ArrayList;
import java.util.List;

import thirdparty.synthesis.component.Component;
import thirdparty.synthesis.component.Components;
import thirdparty.synthesis.util.Z3Util;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Z3Exception;

public class ProgramInstance {

	private static int totalId = 1;

	protected int id = totalId++;
	protected ProgramAbstract program = null;
	public List<Component> components = null;
	public List<Expr> inputVars = new ArrayList<Expr>();
	public Expr outputVar = null;
	public List<Expr> allVars = new ArrayList<Expr>();

	public ProgramInstance() {
	}

	public ProgramInstance(ProgramAbstract pa) {
		program = pa;
	}

	public ProgramAbstract getAbstract() {
		return program;
	}

	public void init(Context ctx) throws Z3Exception {
		// init all the variables
		// function inputs
		for (int i = 0; i < program.inputTypes.size(); i++) {
			Type type = program.inputTypes.get(i);

			Expr var = Z3Util.getVariable(type.varType, ctx,
					"input_" + id + "_" + i);

			allVars.add(var);

			inputVars.add(var);
		}

		// all the other components
		components = Components.getComponents(program.components, ctx);
		for (Component comp : components) {
			allVars.addAll(comp.getVariables());
		}

		// function output
		if (!program.isReturnVoid()) {
			outputVar = Z3Util.getVariable(program.outputType.varType,
					ctx, "result_" + id);
			allVars.add(outputVar);
		}

	}
}
