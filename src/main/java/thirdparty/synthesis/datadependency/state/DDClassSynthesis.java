package thirdparty.synthesis.datadependency.state;

import java.util.List;
import java.util.Map;

import thirdparty.synthesis.basic.Result;
import thirdparty.synthesis.basic.Type;
import thirdparty.synthesis.basic.VarType;
import thirdparty.synthesis.state.BasicClassSynthesis;
import thirdparty.synthesis.state.Call;
import thirdparty.synthesis.state.CallSequence;
import thirdparty.synthesis.state.ClassAbstract;
import thirdparty.synthesis.state.ClassDescriptor;
import thirdparty.synthesis.state.ClassInstance;
import thirdparty.synthesis.state.Lval2Class;
import thirdparty.synthesis.state.MethodAbstract;
import thirdparty.synthesis.state.MethodInstance;
import thirdparty.synthesis.util.StringUtil;
import thirdparty.synthesis.util.Z3Util;

import com.microsoft.z3.ArrayExpr;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Expr;
import com.microsoft.z3.IntExpr;
import com.microsoft.z3.Model;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Status;
import com.microsoft.z3.Z3Exception;

public class DDClassSynthesis extends BasicClassSynthesis {

	public void doSynthesis(ClassDescriptor cd, List<CSPair> csPairs,
			List<CallSequence> initCss) throws Z3Exception {

		ClassAbstract ca = cd.getClassAbstract();
		ca.init(ctx);

		if (csPairs.size() == 0) {
			// generate the initial call sequence pair
			System.out.println();
			System.out.println("Initial call sequence pair generation:");
			generateDistinctClass(cd, csPairs, initCss);
			return;
		}

		Map<MethodAbstract, Result> rets = generateProgram(ca, csPairs);

		if (rets == null) {
			System.out.println("No solution! Components insufficient!");
			return;
		} else {
			System.out.println("Current program:");
			Lval2Class.tranform(rets, ca);
		}

		System.out.println("Start to generate distinct classes...");
		generateDistinctClass(cd, csPairs, initCss);
		System.out.println("Generating distinct classes has done.");
	}

	// generate a program which satisfies the input-output constraints.
	public Map<MethodAbstract, Result> generateProgram(ClassAbstract ca,
			List<CSPair> csPairs) throws Z3Exception {
		Solver solver = ctx.mkSolver();

		addSynthesisConstraints(solver, ca, csPairs);

		long start = System.currentTimeMillis();
		Status status = Z3Util.execute(ctx, solver, 60);
		long end = System.currentTimeMillis();

		System.out.println("Time spent on generating program: " + (end - start)
				+ "ms");

		if (status == Status.SATISFIABLE) {
			Map<MethodAbstract, Result> rets = resolveResult(solver, ca);
			return rets;
		} else {
			return null;
		}
	}

	// thirdparty.synthesis constraints
	public void addSynthesisConstraints(Solver solver, ClassAbstract ca,
			List<CSPair> csPairs) throws Z3Exception {

		addWellFormConstraints(solver, ca);

		// initialize the ClassInstance.
		for (CSPair csPair : csPairs) {
			ClassInstance ci1 = ca.getInstance(csPair.cs1.calls);
			ci1.init(ctx);
			solver.add(funcConstraints(ci1));

			ClassInstance ci2 = ca.getInstance(csPair.cs2.calls);
			ci2.init(ctx);
			solver.add(funcConstraints(ci2));

			addInputOutputConstraints(solver, ci1, ci2, csPair);
		}
	}

	public void addInputOutputConstraints(Solver solver, ClassInstance ci1,
			ClassInstance ci2, CSPair csPair) throws Z3Exception {
		for (int i = 0; i < ci1.methodInstances.size(); i++) {
			MethodInstance mi = ci1.methodInstances.get(i);
			Call call = csPair.cs1.calls.get(i);
			solver.add(inputConstraint(mi, call.ioPair));
		}

		for (int i = 0; i < ci2.methodInstances.size(); i++) {
			MethodInstance mi = ci2.methodInstances.get(i);
			Call call = csPair.cs2.calls.get(i);
			solver.add(inputConstraint(mi, call.ioPair));
		}

		// add the output constraints
		addOutputDependenceConstraint(solver, ci1, ci2, csPair);
	}

	public void addOutputDependenceConstraint(Solver solver, ClassInstance ci1,
			ClassInstance ci2, CSPair csPair) throws Z3Exception {

		for (int i = 0; i < ci1.methodInstances.size(); i++) {
			MethodInstance mi1 = ci1.methodInstances.get(i);
			Call call1 = csPair.cs1.calls.get(i);

			MethodInstance mi2 = ci2.methodInstances.get(i);
			Call call2 = csPair.cs2.calls.get(i);

			if (!mi1.getAbstract().isReturnVoid()) {
				if (mi1.getAbstract().outputType.varType == VarType.ARRAY) {
					int[] o1 = (int[]) call1.ioPair.output;
					int[] o2 = (int[]) call2.ioPair.output;
					ArrayExpr arr1 = (ArrayExpr) mi1.outputVar;
					ArrayExpr arr2 = (ArrayExpr) mi2.outputVar;

					if (o1.length == o2.length) {
						BoolExpr ret = ctx.mkTrue();
						for (int j = 0; j < o1.length; j++) {
							if (o1[j] == o2[j]) {
								ret = ctx.mkAnd(ret, ctx.mkEq(
										ctx.mkSelect(arr1, ctx.mkInt(j)),
										ctx.mkSelect(arr2, ctx.mkInt(j))));
							} else {
								ret = ctx.mkAnd(ret, ctx.mkNot(ctx.mkEq(
										ctx.mkSelect(arr1, ctx.mkInt(j)),
										ctx.mkSelect(arr2, ctx.mkInt(j)))));
							}
						}
						solver.add(ret);
					} else {
						BoolExpr o = ctx.mkNot(StringUtil.equal(ctx,
								mi1.getAbstract().outputType, mi1.outputVar,
								mi2.outputVar));
						solver.add(o);
					}
				} else {
					if (call1.ioPair.output != call2.ioPair.output) {
						BoolExpr o = ctx.mkNot(ctx.mkEq(mi1.outputVar,
								mi2.outputVar));
						solver.add(o);
					} else {
						BoolExpr o = ctx.mkEq(mi1.outputVar, mi2.outputVar);
						solver.add(o);
					}
				}
			}
		}
	}

	// generate distinct class
	public Object[] generateDistinctClass(ClassDescriptor cd,
			List<CSPair> csPairs, List<CallSequence> initCss)
			throws Z3Exception {
		Solver solver = ctx.mkSolver();

		// First class
		ClassAbstract clazz1 = cd.getClassAbstract();
		clazz1.init(ctx);
		addSynthesisConstraints(solver, clazz1, csPairs);

		// Second class
		ClassAbstract clazz2 = cd.getClassAbstract();
		clazz2.init(ctx);
		addSynthesisConstraints(solver, clazz2, csPairs);

		// function constraints for first class
		ClassInstance ci11 = clazz1.getInstance(initCss.get(0).calls);
		ci11.init(ctx);
		solver.add(funcConstraints(ci11));
		ClassInstance ci12 = clazz1.getInstance(initCss.get(0).calls);
		ci12.init(ctx);
		solver.add(funcConstraints(ci12));

		// function constraints for second program
		ClassInstance ci21 = clazz2.getInstance(initCss.get(0).calls);
		ci21.init(ctx);
		solver.add(funcConstraints(ci21));
		ClassInstance ci22 = clazz2.getInstance(initCss.get(0).calls);
		ci22.init(ctx);
		solver.add(funcConstraints(ci22));

		// input - output constraint for two program for same inputs
		for (int i = 0; i < initCss.get(0).calls.size(); i++) {
			MethodInstance mi11 = ci11.methodInstances.get(i);
			MethodInstance mi12 = ci12.methodInstances.get(i);
			MethodInstance mi21 = ci21.methodInstances.get(i);
			MethodInstance mi22 = ci22.methodInstances.get(i);

			for (int j = 0; j < mi11.getAbstract().inputTypes.size(); j++) {
				solver.add(ctx.mkEq(mi11.inputVars.get(j),
						mi21.inputVars.get(j)));
				solver.add(ctx.mkEq(mi12.inputVars.get(j),
						mi22.inputVars.get(j)));

				// only one termination character for a string.
				if (mi11.getAbstract().inputTypes.get(j).varType == VarType.ARRAY) {
					solver.add(StringUtil.wellFormedStringBound(ctx,
							(ArrayExpr) mi11.inputVars.get(j)));
					solver.add(StringUtil.wellFormedStringBound(ctx,
							(ArrayExpr) mi21.inputVars.get(j)));
				}
			}
		}

		// use the representative inputs
		if (initCss != null && initCss.size() > 0) {
			BoolExpr existEqual = ctx.mkFalse();

			for (CallSequence cs : initCss) {
				BoolExpr equal = ctx.mkTrue();
				for (int i = 0; i < cs.calls.size(); i++) {
					Call call = cs.calls.get(i);
					MethodInstance mi = ci11.methodInstances.get(i);
					for (int j = 0; j < call.ioPair.inputs.length; j++) {
						Type type = mi.getAbstract().inputTypes.get(j);
						Expr inVar = mi.inputVars.get(j);
						Object input = call.ioPair.inputs[j];

						if (type.varType == VarType.INTEGER) {
							int in = (Integer) input;
							equal = ctx.mkAnd(equal,
									ctx.mkEq(inVar, ctx.mkInt(in)));
						} else if (type.varType == VarType.BOOLEAN) {

						} else {
							int[] arr = (int[]) input;
							ArrayExpr arrVar = (ArrayExpr) inVar;
							for (int k = 0; k < arr.length; k++) {
								equal = ctx.mkAnd(
										equal,
										ctx.mkEq(
												ctx.mkSelect(arrVar,
														ctx.mkInt(k)),
												ctx.mkInt(arr[k])));
							}
							equal = ctx.mkAnd(
									equal,
									ctx.mkEq(
											ctx.mkSelect(arrVar,
													ctx.mkInt(arr.length)),
											ctx.mkInt(0)));
						}
					}
				}

				existEqual = ctx.mkOr(existEqual, equal);
			}

			solver.add(existEqual);
		}

		IntExpr delta = ctx.mkIntConst("delta");
		BoolExpr delta1 = ctx.mkGt(delta, ctx.mkInt(0));
		BoolExpr delta2 = ctx.mkLt(delta, ctx.mkInt(2));
		BoolExpr deltaCons = ctx.mkAnd(delta1, delta2);
		solver.add(deltaCons);

		int inputNum = 0;
		for (MethodInstance mi : ci11.methodInstances) {
			inputNum += mi.inputVars.size();
		}

		BoolExpr inputsCons = ctx.mkBool(false);
		for (int neIndex = 0; neIndex < inputNum; neIndex++) {
			int curInput = 0;
			BoolExpr inputEqual = ctx.mkBool(true);
			for (int iMi = 0; iMi < ci11.methodInstances.size(); iMi++) {
				MethodInstance mi11 = ci11.methodInstances.get(iMi);
				MethodInstance mi12 = ci12.methodInstances.get(iMi);

				for (int iInput = 0; iInput < mi11.getAbstract().inputTypes
						.size(); iInput++) {
					Type type = mi11.getAbstract().inputTypes.get(iInput);
					if (curInput == neIndex) {
						BoolExpr nNotEqual = null;
						if (type.varType == VarType.INTEGER) {
							nNotEqual = ctx.mkEq(mi11.inputVars.get(iInput),
									ctx.mkAdd((IntExpr) mi12.inputVars
											.get(iInput), delta));
						} else {
							nNotEqual = StringUtil.editDistanceBound(ctx,
									(ArrayExpr) mi11.inputVars.get(iInput),
									(ArrayExpr) mi12.inputVars.get(iInput));
						}
						inputEqual = ctx.mkAnd(inputEqual, nNotEqual);
					} else {
						inputEqual = ctx.mkAnd(inputEqual, ctx.mkEq(
								mi11.inputVars.get(iInput),
								mi12.inputVars.get(iInput)));
					}

					curInput++;
				}
			}
			inputsCons = ctx.mkOr(inputsCons, inputEqual);
		}

		solver.add(inputsCons);

		BoolExpr outputCons = ctx.mkBool(false);
		for (int i = 0; i < ci11.methodInstances.size(); i++) {
			MethodInstance mi11 = ci11.methodInstances.get(i);
			MethodInstance mi12 = ci12.methodInstances.get(i);
			MethodInstance mi21 = ci21.methodInstances.get(i);
			MethodInstance mi22 = ci22.methodInstances.get(i);
			BoolExpr equal = StringUtil.equal(ctx,
					mi11.getAbstract().outputType, mi11.outputVar,
					mi12.outputVar);
			BoolExpr nEqual = ctx.mkNot(StringUtil.equal(ctx,
					mi11.getAbstract().outputType, mi21.outputVar,
					mi22.outputVar));
			outputCons = ctx.mkOr(outputCons, ctx.mkAnd(equal, nEqual));

		}
		solver.add(outputCons);

		long start = System.currentTimeMillis();
		Status status = Z3Util.execute(ctx, solver, 60);
		long end = System.currentTimeMillis();

		System.out.println("Time spent on generating distinct class: "
				+ (end - start) + "ms");

		if (status == Status.SATISFIABLE) {
			printDistinctClasses(solver, clazz1, clazz2);

			Model model = solver.getModel();

			System.out.println("Call sequence 1:");
			for (int i = 0; i < ci11.methodInstances.size(); i++) {
				MethodInstance mi1 = ci11.methodInstances.get(i);
				Object[] newInputs1 = resolveInput(model, mi1);
				System.out.println(mi1.getAbstract().methodName + ": "
						+ Z3Util.printObjects(newInputs1));

				// resolveOutputs(model, mi1, mi2);
				// resolveStates(model, mi1, mi2);
			}

			System.out.println();
			System.out.println("Call sequence 2:");
			for (int i = 0; i < ci21.methodInstances.size(); i++) {
				MethodInstance mi2 = ci12.methodInstances.get(i);
				Object[] newInputs2 = resolveInput(model, mi2);
				System.out.println(mi2.getAbstract().methodName + ": "
						+ Z3Util.printObjects(newInputs2));

				// resolveOutputs(model, mi1, mi2);
				// resolveStates(model, mi1, mi2);
			}
		}
		return null;
	}
}