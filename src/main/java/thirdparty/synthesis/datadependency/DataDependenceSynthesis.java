package thirdparty.synthesis.datadependency;

import java.util.ArrayList;
import java.util.List;

import thirdparty.synthesis.basic.BasicSynthesis;
import thirdparty.synthesis.basic.IOPair;
import thirdparty.synthesis.basic.Lval2Prog;
import thirdparty.synthesis.basic.ProgramAbstract;
import thirdparty.synthesis.basic.ProgramInstance;
import thirdparty.synthesis.basic.Result;
import thirdparty.synthesis.basic.Specification;
import thirdparty.synthesis.basic.Type;
import thirdparty.synthesis.basic.VarType;
import thirdparty.synthesis.component.Component;
import thirdparty.synthesis.component.IfElseComponent;
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

public class DataDependenceSynthesis extends BasicSynthesis {

	public void doSynthesis(List<Type> inputTypes, Type outputType,
			List<Component> comps, List<IOPair> ioPairs) throws Z3Exception {

		List<DDPair> ddPairs = getDDPairs(ioPairs);

		printDDPairs(ddPairs);

		ProgramAbstract program = new ProgramAbstract(comps, inputTypes,
				outputType);
		program.init(ctx);

		System.out.println("Start to generate a program...");
		Result res = generateProgram(program, ddPairs);
		System.out.println("Generating a program has done.");

		if (res == null) {
			System.out.println("No solution! Components insufficient!");
			return;
		} else {
			System.out.println("Current program:");
			Lval2Prog.tranform(res, program, true);
		}
		System.out.println("Start to generate distinct programs...");
		List<Object[]> newInputs = generateDistinctProgram(program, ddPairs,
				null);
		System.out.println("Generating distinct programs have done.");

		if (newInputs != null) {
			System.out.println("New inputs:");
			System.out.println("Input 1:"
					+ Z3Util.printObjects(newInputs.get(0)));
			System.out.println("Input 2:"
					+ Z3Util.printObjects(newInputs.get(1)));
		}
	}

	public void doSynthesis(List<Type> inputTypes, Type outputType,
			List<Component> comps, Specification spec) throws Z3Exception {
		doSynthesis(inputTypes, outputType, comps, spec, null);
	}

	public void doSynthesis(List<Type> inputTypes, Type outputType,
			List<Component> comps, Specification spec, List<Object[]> repInputs)
			throws Z3Exception {

		List<DDPair> ddPairs = new ArrayList<DDPair>();

		ProgramAbstract program = new ProgramAbstract(comps, inputTypes,
				outputType);
		program.init(ctx);

		// generate the initial dd pair
		System.out.println();
		System.out.println("Initial dd pair generation:");
		List<Object[]> inputs = generateDistinctProgram(program, ddPairs,
				repInputs);
		DDPair initDDPair = new DDPair(spec.getIO(inputs.get(0)),
				spec.getIO(inputs.get(1)));
		ddPairs.add(initDDPair);

		for (int iter = 1;; iter++) {

			System.out.println();
			System.out.println("Iteration " + iter + ":");
			printDDPairs(ddPairs);

			Result res = generateProgram(program, ddPairs);

			if (res == null) {
				System.out.println("No solution! Components insufficient!");
				return;
			} else {
				System.out.println("Current program:");
				Lval2Prog.tranform(res, program, true);
			}

			List<Object[]> newInputs = generateDistinctProgram(program,
					ddPairs, repInputs);

			if (newInputs != null) {

				DDPair newDDPair = new DDPair(spec.getIO(newInputs.get(0)),
						spec.getIO(newInputs.get(1)));
				ddPairs.add(newDDPair);

				System.out.println("New inputs:");
				System.out.println("Input 1:"
						+ Z3Util.printObjects(newInputs.get(0)));
				System.out.println("Input 2:"
						+ Z3Util.printObjects(newInputs.get(1)));
				System.out.println();
			} else {
				break;
			}
		}
	}

	// generate a program which satisfies the input-output constraints.
	public Result generateProgram(ProgramAbstract program, List<DDPair> ddPairs)
			throws Z3Exception {
		Solver solver = ctx.mkSolver();

		addSynthesisConstraints(solver, program, ddPairs);

		Status status = Z3Util.execute(ctx, solver, 60);

		if (status == Status.SATISFIABLE) {
			Result res = resolveResult(solver, program);
			return res;
		} else {
			return null;
		}
	}

	// thirdparty.synthesis constraints
	public void addSynthesisConstraints(Solver solver, ProgramAbstract program,
			List<DDPair> ddPairs) throws Z3Exception {

		addWellFormConstraint(solver, program);

		// TODO
		boolean[] pathHash = { true, true, true, true, true, true, true, true,
				true, true, true, true, true, true, true, true };

		for (int i = 0; i < ddPairs.size(); i++) {
			DDPair ddPair = ddPairs.get(i);

			ProgramInstance pi1 = program.getInstance();
			pi1.init(ctx);

			solver.add(funcConstraint(pi1));

			ProgramInstance pi2 = program.getInstance();
			pi2.init(ctx);

			solver.add(funcConstraint(pi2));

			addInputOutputDependenceConstraints(solver, pi1, pi2, ddPair,
					pathHash[i]);
		}
	}

	// input output dependence constraints
	public void addInputOutputDependenceConstraints(Solver solver,
			ProgramInstance pi1, ProgramInstance pi2, DDPair ddPair,
			boolean samePath) throws Z3Exception {

		solver.add(inputConstraint(pi1, ddPair.io1));
		solver.add(inputConstraint(pi2, ddPair.io2));

		if (pi1.getAbstract().outputType.varType == VarType.ARRAY) {
			int[] o1 = (int[]) ddPair.io1.output;
			int[] o2 = (int[]) ddPair.io2.output;
			ArrayExpr arr1 = (ArrayExpr) pi1.outputVar;
			ArrayExpr arr2 = (ArrayExpr) pi2.outputVar;

			if (o1.length == o2.length) {
				BoolExpr ret = ctx.mkTrue();
				for (int i = 0; i < o1.length; i++) {
					if (o1[i] == o2[i]) {
						ret = ctx.mkAnd(
								ret,
								ctx.mkEq(ctx.mkSelect(arr1, ctx.mkInt(i)),
										ctx.mkSelect(arr2, ctx.mkInt(i))));
					} else {
						ret = ctx.mkAnd(
								ret,
								ctx.mkNot(ctx.mkEq(
										ctx.mkSelect(arr1, ctx.mkInt(i)),
										ctx.mkSelect(arr2, ctx.mkInt(i)))));
					}
				}
				solver.add(ret);
			} else {
				BoolExpr o = ctx.mkNot(StringUtil.equal(ctx,
						pi1.getAbstract().outputType, pi1.outputVar,
						pi2.outputVar));
				solver.add(o);
			}
		} else {
			if (ddPair.io1.output != ddPair.io2.output) {
				BoolExpr o = ctx.mkNot(ctx.mkEq(pi1.outputVar, pi2.outputVar));
				solver.add(o);
			} else {
				BoolExpr o = ctx.mkEq(pi1.outputVar, pi2.outputVar);
				solver.add(o);
			}
		}

		/*
		 * if (samePath) { // each program follow the same branches for (int i =
		 * 0; i < pi1.program.components.size(); i++) { Component comp =
		 * pi1.program.components.get(i); if (comp instanceof IfElseComponent) {
		 * BoolExpr cond1 = (BoolExpr) pi1.components.get(i)
		 * .getVariables().get(0); BoolExpr cond2 = (BoolExpr)
		 * pi2.components.get(i) .getVariables().get(0);
		 * solver.add(ctx.mkEq(cond1, cond2)); } } } else { // two programs
		 * follow the different branches on input I1 BoolExpr diffPath =
		 * ctx.mkBool(false); for (int i = 0; i < pi1.program.components.size();
		 * i++) { Component comp = pi1.program.components.get(i); if (comp
		 * instanceof IfElseComponent) { BoolExpr cond1 = (BoolExpr)
		 * pi1.components.get(i) .getVariables().get(0);
		 * 
		 * BoolExpr cond2 = (BoolExpr) pi2.components.get(i)
		 * .getVariables().get(0);
		 * 
		 * diffPath = ctx.mkOr(diffPath, ctx.mkNot(ctx.mkEq(cond1, cond2))); } }
		 * solver.add(diffPath); }
		 */
	}

	// generate distinct program
	public List<Object[]> generateDistinctProgram(ProgramAbstract program,
			List<DDPair> ddPairs, List<Object[]> repInputs) throws Z3Exception {
		Solver solver = ctx.mkSolver();

		// First program
		ProgramAbstract prog1 = new ProgramAbstract(program.components,
				program.inputTypes, program.outputType);
		prog1.init(ctx);
		addSynthesisConstraints(solver, prog1, ddPairs);

		// Second program
		ProgramAbstract prog2 = new ProgramAbstract(program.components,
				program.inputTypes, program.outputType);
		prog2.init(ctx);
		addSynthesisConstraints(solver, prog2, ddPairs);

		// function constraints for first program
		ProgramInstance pi11 = prog1.getInstance();
		pi11.init(ctx);
		solver.add(funcConstraint(pi11));

		ProgramInstance pi12 = prog1.getInstance();
		pi12.init(ctx);
		solver.add(funcConstraint(pi12));

		// function constraints for second program
		ProgramInstance pi21 = prog2.getInstance();
		pi21.init(ctx);
		solver.add(funcConstraint(pi21));

		ProgramInstance pi22 = prog2.getInstance();
		pi22.init(ctx);
		solver.add(funcConstraint(pi22));

		// input - output constraint for two program for different inputs
		for (int i = 0; i < program.inputTypes.size(); i++) {
			solver.add(ctx.mkEq(pi11.inputVars.get(i), pi21.inputVars.get(i)));
			solver.add(ctx.mkEq(pi12.inputVars.get(i), pi22.inputVars.get(i)));

			// only one termination character for a string.
			if (program.inputTypes.get(i).varType == VarType.ARRAY) {
				solver.add(StringUtil.wellFormedStringBound(ctx,
						(ArrayExpr) pi11.inputVars.get(i)));

				solver.add(StringUtil.wellFormedStringBound(ctx,
						(ArrayExpr) pi12.inputVars.get(i)));
			}
		}

		// use the representative inputs
		if (repInputs != null && repInputs.size() > 0) {
			BoolExpr existEqual = ctx.mkFalse();

			for (Object[] inputs : repInputs) {
				BoolExpr equal = ctx.mkTrue();
				for (int i = 0; i < inputs.length; i++) {
					Type type = program.inputTypes.get(i);
					Expr inVar = pi11.inputVars.get(i);
					Object input = inputs[i];

					if (type.varType == VarType.INTEGER) {
						int in = (Integer) input;
						equal = ctx
								.mkAnd(equal, ctx.mkEq(inVar, ctx.mkInt(in)));
					} else if (type.varType == VarType.BOOLEAN) {

					} else {
						int[] arr = (int[]) input;
						ArrayExpr arrVar = (ArrayExpr) inVar;
						for (int j = 0; j < arr.length; j++) {
							equal = ctx.mkAnd(equal, ctx.mkEq(
									ctx.mkSelect(arrVar, ctx.mkInt(j)),
									ctx.mkInt(arr[j])));
						}
						equal = ctx.mkAnd(
								equal,
								ctx.mkEq(
										ctx.mkSelect(arrVar,
												ctx.mkInt(arr.length)),
										ctx.mkInt(0)));
					}
				}

				existEqual = ctx.mkOr(existEqual, equal);
			}

			solver.add(existEqual);
		}

		// two inputs are different
		/*
		 * // this constraint is too relax, and the convergence is very slow.
		 * BoolExpr inputsCons = ctx.mkBool(false); for (int i = 0; i <
		 * program.inputTypes.size(); i++) { inputsCons = ctx.mkOr( inputsCons,
		 * ctx.mkNot(ctx.mkEq(pi11.inputVars.get(i), pi12.inputVars.get(i)))); }
		 */
		IntExpr delta = ctx.mkIntConst("delta");
		BoolExpr delta1 = ctx.mkGt(delta, ctx.mkInt(0));
		BoolExpr delta2 = ctx.mkLt(delta, ctx.mkInt(2));
		BoolExpr deltaCons = ctx.mkAnd(delta1, delta2);
		solver.add(deltaCons);

		BoolExpr inputsCons = ctx.mkBool(false);
		for (int i = 0; i < program.inputTypes.size(); i++) {
			Type type = program.inputTypes.get(i);
			BoolExpr iNotEqual = null;
			if (type.varType == VarType.INTEGER) {
				iNotEqual = ctx.mkEq(pi11.inputVars.get(i),
						ctx.mkAdd((IntExpr) pi12.inputVars.get(i), delta));
			} else {
				iNotEqual = StringUtil.editDistanceBound(ctx,
						(ArrayExpr) pi11.inputVars.get(i),
						(ArrayExpr) pi12.inputVars.get(i));
			}

			BoolExpr otherEqual = ctx.mkBool(true);
			for (int j = 0; j < program.inputTypes.size(); j++) {
				if (j != i) {
					otherEqual = ctx.mkAnd(
							otherEqual,
							ctx.mkEq(pi11.inputVars.get(j),
									pi12.inputVars.get(j)));
				}
			}
			inputsCons = ctx.mkOr(inputsCons, ctx.mkAnd(iNotEqual, otherEqual));
		}

		solver.add(inputsCons);

		solver.push();

		// each program follow the same branches
		for (int i = 0; i < program.components.size(); i++) {
			Component comp = program.components.get(i);
			if (comp instanceof IfElseComponent) {
				BoolExpr cond11 = (BoolExpr) pi11.components.get(i)
						.getVariables().get(0);
				BoolExpr cond12 = (BoolExpr) pi12.components.get(i)
						.getVariables().get(0);
				solver.add(ctx.mkEq(cond11, cond12));

				BoolExpr cond21 = (BoolExpr) pi21.components.get(i)
						.getVariables().get(0);
				BoolExpr cond22 = (BoolExpr) pi22.components.get(i)
						.getVariables().get(0);
				solver.add(ctx.mkEq(cond21, cond22));
			}
		}

		solver.add(StringUtil.equal(ctx, program.outputType, pi11.outputVar,
				pi12.outputVar));
		solver.add(ctx.mkNot(StringUtil.equal(ctx, program.outputType,
				pi21.outputVar, pi22.outputVar)));

		// long start = System.currentTimeMillis();
		Status status = Z3Util.execute(ctx, solver, 60);
		// long end = System.currentTimeMillis();

		// System.out.println("Time spent on generating distinct program: "
		// + (end - start) + "ms");

		if (status != Status.SATISFIABLE) {
			solver.pop();

			// I1 & I2 follow the same branches in L1

			for (int i = 0; i < program.components.size(); i++) {
				Component comp = program.components.get(i);
				if (comp instanceof IfElseComponent) {
					BoolExpr cond11 = (BoolExpr) pi11.components.get(i)
							.getVariables().get(0);
					BoolExpr cond12 = (BoolExpr) pi12.components.get(i)
							.getVariables().get(0);
					solver.add(ctx.mkEq(cond11, cond12));
				}
			}

			// I1 & I2 follow the different branches in L1
			boolean hasIf = false;
			BoolExpr diffPath = ctx.mkBool(false);
			for (int i = 0; i < program.components.size(); i++) {
				Component comp = program.components.get(i);
				if (comp instanceof IfElseComponent) {
					hasIf = true;

					BoolExpr cond21 = (BoolExpr) pi21.components.get(i)
							.getVariables().get(0);

					BoolExpr cond22 = (BoolExpr) pi22.components.get(i)
							.getVariables().get(0);

					diffPath = ctx.mkOr(diffPath,
							ctx.mkNot(ctx.mkEq(cond21, cond22)));
				}
			}

			if (hasIf) {
				solver.add(diffPath);
				status = solver.check();
			}
		}

		if (status == Status.SATISFIABLE) {
			printDistinctPrograms(solver, prog1, prog2);

			Model model = solver.getModel();

			List<Object[]> inputs = new ArrayList<Object[]>();

			Object[] newInput1 = resolveInput(model, pi11);
			inputs.add(newInput1);

			Object[] newInput2 = resolveInput(model, pi12);
			inputs.add(newInput2);

			return inputs;
		}
		return null;
	}

	public List<DDPair> getDDPairs(List<IOPair> ioPairs) {
		List<DDPair> ddPairs = new ArrayList<DDPair>();
		// just have one different input

		for (int i = 0; i < ioPairs.size(); i++) {
			IOPair one = ioPairs.get(i);
			for (int j = i + 1; j < ioPairs.size(); j++) {
				IOPair two = ioPairs.get(j);

				Object[] inputs1 = one.inputs;
				Object[] inputs2 = two.inputs;
				int num = 0;
				for (int k = 0; k < inputs1.length; k++) {
					if (inputs1[k] instanceof Integer) {
						if (!inputs1[k].equals(inputs2[k])) {
							num++;
							if ((Math.abs((Integer) inputs1[k]
									- (Integer) inputs2[k]) != 1)) {
								// discard this pair.
								num++;
								break;
							}
						}
					} else {
						int[] arr1 = (int[]) inputs1[k];
						int[] arr2 = (int[]) inputs2[k];
						num += StringUtil.editDistance(arr1, arr2);
					}
				}
				if (num > 1) {
					continue;
				}

				ddPairs.add(new DDPair(one, two));
			}
		}
		return ddPairs;
	}

	public void addIOPair(List<IOPair> ioPairs, IOPair ioPair) {
		for (IOPair io : ioPairs) {
			boolean same = true;
			for (int i = 0; i < io.inputs.length; i++) {
				if (!io.inputs[i].equals(ioPair.inputs[i])) {
					same = false;
					break;
				}
			}
			if (same) {
				return;
			}
		}
		ioPairs.add(ioPair);
	}

	public void printDDPairs(List<DDPair> ddPairs) {
		System.out.println("data dependence pairs:");
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < ddPairs.size(); i++) {
			DDPair pair = ddPairs.get(i);
			sb.append(pair);

			if (i < ddPairs.size() - 1) {
				sb.append(", ");
			}
		}
		System.out.println(sb);
	}
}