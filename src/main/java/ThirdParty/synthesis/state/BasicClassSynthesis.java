package ThirdParty.synthesis.state;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ThirdParty.synthesis.basic.BasicSynthesis;
import ThirdParty.synthesis.basic.Result;
import ThirdParty.synthesis.basic.Type;
import ThirdParty.synthesis.basic.VarType;
import ThirdParty.synthesis.util.Z3Util;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Model;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Z3Exception;

public class BasicClassSynthesis extends BasicSynthesis {

	// well formedness constrains
	public void addWellFormConstraints(Solver solver, ClassAbstract ca)
			throws Z3Exception {

		for (MethodAbstract method : ca.methods) {
			addWellFormConstraint(solver, method);
		}
	}

	// function constraints
	public BoolExpr funcConstraints(ClassInstance ci) throws Z3Exception {

		BoolExpr ret = ctx.mkBool(true);

		StateInstance si = new StateInstance(ci.calssAbstract.stateAbstract);
		for (MethodInstance mi : ci.methodInstances) {
			ret = ctx.mkAnd(ret, funcConstraint(mi));

			if (si.stateVars != null) {
				for (int i = 0; i < si.stateVars.size(); i++) {
					ret = ctx.mkAnd(ret, ctx.mkEq(si.stateVars.get(i),
							mi.inStateVars.get(i)));
				}
			}

			si = mi.getAbstract().classAbstract.stateAbstract
					.getInstance(mi.outStateVars);
		}

		return ret;
	}

	public StateInstance addStateConstraint(Solver solver, MethodInstance mi,
			StateInstance si) throws Z3Exception {
		if (si.stateVars != null) {
			for (int i = 0; i < si.stateVars.size(); i++) {
				solver.add(ctx.mkEq(si.stateVars.get(i), mi.inStateVars.get(i)));
			}
		}

		StateInstance newSI = mi.getAbstract().classAbstract.stateAbstract
				.getInstance(mi.outStateVars);
		return newSI;
	}

	public void printDistinctClasses(Solver solver, ClassAbstract ca1,
			ClassAbstract ca2) throws Z3Exception {

		System.out.println("Distinct class 1: ============== ");
		Map<MethodAbstract, Result> rets1 = resolveResult(solver, ca1);
		Lval2Class.tranform(rets1, ca1);

		System.out.println("Distinct class 2: ============== ");
		Map<MethodAbstract, Result> rets2 = resolveResult(solver, ca2);
		Lval2Class.tranform(rets2, ca2);
	}

	public Map<MethodAbstract, Result> resolveResult(Solver solver,
			ClassAbstract ca) throws Z3Exception {
		Map<MethodAbstract, Result> rets = new HashMap<MethodAbstract, Result>();
		for (MethodAbstract ma : ca.methods) {
			Result ret = resolveResult(solver, ma);
			rets.put(ma, ret);
		}

		return rets;
	}

	@SuppressWarnings("unchecked")
	public void resolveStates(Model model, MethodInstance mi1,
			MethodInstance mi2) throws Z3Exception {

		List<Type> types = mi1.getAbstract().classAbstract.stateAbstract.stateTypes;
		for (int i = 0; i < types.size(); i++) {
			Type type = types.get(i);

			Object object1 = Z3Util.getValue(ctx, model, type.varType,
					mi1.outStateVars.get(i));
			Object object2 = Z3Util.getValue(ctx, model, type.varType,
					mi2.outStateVars.get(i));

			if (type.varType == VarType.INTEGER
					|| type.varType == VarType.BOOLEAN
					|| type.varType == VarType.DOUBLE) {
				System.out.println("  --State." + i + " 1:" + object1);
				System.out.println("  --State." + i + " 2:" + object2);
			} else if (type.varType == VarType.ARRAY) {
				System.out.println("  --State." + i + " 1:"
						+ Z3Util.printString((int[]) object1));
				System.out.println("  --State." + i + " 2:"
						+ Z3Util.printString((int[]) object2));
			}
			if (type.varType == VarType.HASH) {
				System.out.println("  --State." + i + " 1:"
						+ Z3Util.printMap((Map<Integer, int[]>) object1));
				System.out.println("  --State." + i + " 2:"
						+ Z3Util.printMap((Map<Integer, int[]>) object2));

			}
		}
	}
}