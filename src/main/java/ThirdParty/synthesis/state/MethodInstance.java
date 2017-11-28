package ThirdParty.synthesis.state;

import java.util.ArrayList;
import java.util.List;

import ThirdParty.synthesis.basic.ProgramInstance;
import ThirdParty.synthesis.basic.Type;
import ThirdParty.synthesis.util.Z3Util;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Z3Exception;

public class MethodInstance extends ProgramInstance {

	public List<Expr> inStateVars = new ArrayList<Expr>();
	public List<Expr> outStateVars = new ArrayList<Expr>();

	public MethodInstance(MethodAbstract ma) {
		program = ma;
	}

	public MethodAbstract getAbstract() {
		return (MethodAbstract) program;
	}

	public void init(Context ctx) throws Z3Exception {
		// input states
		if (getAbstract().isInit == false) {
			for (int i = 0; i < getAbstract().classAbstract.stateAbstract.stateTypes
					.size(); i++) {
				Type type = getAbstract().classAbstract.stateAbstract.stateTypes
						.get(i);
				Expr var = Z3Util.getVariable(type.varType, ctx,
						"method_state_in_" + id + "_" + i);
				allVars.add(var);
				inStateVars.add(var);
			}
		}

		// program part
		super.init(ctx);

		// output states
		for (int i = 0; i < getAbstract().classAbstract.stateAbstract.stateTypes
				.size(); i++) {
			Type type = getAbstract().classAbstract.stateAbstract.stateTypes
					.get(i);
			Expr var = Z3Util.getVariable(type.varType, ctx,
					"method_state_out_" + id + "_" + i);
			allVars.add(var);
			outStateVars.add(var);
		}
	}
}
