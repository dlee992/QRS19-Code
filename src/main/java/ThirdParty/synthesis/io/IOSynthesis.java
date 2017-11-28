package ThirdParty.synthesis.io;

import java.util.ArrayList;
import java.util.List;

import ThirdParty.synthesis.basic.BasicSynthesis;
import ThirdParty.synthesis.basic.IOPair;
import ThirdParty.synthesis.basic.Lval2Prog;
import ThirdParty.synthesis.basic.ProgramAbstract;
import ThirdParty.synthesis.basic.ProgramInstance;
import ThirdParty.synthesis.basic.Result;
import ThirdParty.synthesis.basic.Specification;
import ThirdParty.synthesis.basic.Type;
import ThirdParty.synthesis.basic.VarType;
import ThirdParty.synthesis.component.Component;
import ThirdParty.synthesis.util.StringUtil;
import ThirdParty.synthesis.util.Z3Util;

import com.microsoft.z3.ArrayExpr;
import com.microsoft.z3.Model;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Status;
import com.microsoft.z3.Z3Exception;

public class IOSynthesis extends BasicSynthesis {

	public void doSynthesis(List<Type> inputTypes, Type outputType,
			List<Component> comps, List<IOPair> ioPairs) throws Z3Exception {

		ProgramAbstract program = new ProgramAbstract(comps, inputTypes,
				outputType);
		program.init(ctx);

		System.out.println("Start to generate a program...");
		Result res = generateProgram(program, ioPairs);
		System.out.println("Generating a program has done.");

		if (res == null) {
			System.out.println("No solution! Components insufficient!");
			return;
		} else {
			System.out.println("Current program:");
			Lval2Prog.tranform(res, program, true);
		}
		System.out.println("Start to generate distinct programs...");
		Object[] newInputs = generateDistinctProgram(program, ioPairs);
		System.out.println("Generating distinct programs have done.");

		if (newInputs != null) {
			System.out.println("New inputs:" + Z3Util.printObjects(newInputs));
		}
	}

	public void doSynthesis(List<Type> inputTypes, Type outputType,
			List<Component> comps, Specification spec) throws Z3Exception {

		List<IOPair> ioPairs = new ArrayList<IOPair>();
		ioPairs.add(spec.getInitIO(inputTypes));

		for (int iter = 1;; iter++) {

			System.out.println();
			System.out.println("Iteration " + iter + ":");
			printIOPairs(ioPairs);

			ProgramAbstract program = new ProgramAbstract(comps, inputTypes,
					outputType);
			program.init(ctx);

			Result res = generateProgram(program, ioPairs);

			if (res == null) {
				System.out.println("No solution! Components insufficient!");
				return;
			} else {
				System.out.println("Current program:");
				Lval2Prog.tranform(res, program, true);
			}

			Object[] newInputs = generateDistinctProgram(program, ioPairs);

			if (newInputs != null) {
				ioPairs.add(spec.getIO(newInputs));

				System.out
						.print("New inputs:" + Z3Util.printObjects(newInputs));
				System.out.println();
			} else {
				break;
			}
		}
	}

	// generate a program which satisfies the input-output constraints.
	public Result generateProgram(ProgramAbstract program, List<IOPair> ioPairs)
			throws Z3Exception {
		Solver solver = ctx.mkSolver();

		addSynthesisConstraints(solver, program, ioPairs);

		long start = System.currentTimeMillis();
		// Status status = solver.check();
		Status status = Z3Util.execute(ctx, solver, 1);
		long end = System.currentTimeMillis();

		System.out.println("Time spent on generating program: " + (end - start)
				+ "ms");

		if (status == Status.SATISFIABLE) {
			Result res = resolveResult(solver, program);
			return res;
		} else {
			return null;
		}
	}

	// ThirdParty.synthesis constraints
	public void addSynthesisConstraints(Solver solver, ProgramAbstract program,
			List<IOPair> ioPairs) throws Z3Exception {

		addWellFormConstraint(solver, program);

		for (IOPair pair : ioPairs) {
			ProgramInstance pi = program.getInstance();
			pi.init(ctx);

			solver.add(funcConstraint(pi));

			addInputOutputConstraint(solver, pi, pair);
		}
	}

	// input output constraints
	public void addInputOutputConstraint(Solver solver, ProgramInstance pi,
			IOPair pair) throws Z3Exception {

		solver.add(inputConstraint(pi, pair));

		solver.add(outputConstraint(pi, pair));
	}

	// generate distinct program
	public Object[] generateDistinctProgram(ProgramAbstract program,
			List<IOPair> ioPairs) throws Z3Exception {
		Solver solver = ctx.mkSolver();

		// First program
		ProgramAbstract prog1 = new ProgramAbstract(program.components,
				program.inputTypes, program.outputType);
		prog1.init(ctx);
		addSynthesisConstraints(solver, prog1, ioPairs);

		// Second program
		ProgramAbstract prog2 = new ProgramAbstract(program.components,
				program.inputTypes, program.outputType);
		prog2.init(ctx);
		addSynthesisConstraints(solver, prog2, ioPairs);

		// function constraints for first program
		ProgramInstance pi1 = prog1.getInstance();
		pi1.init(ctx);
		solver.add(funcConstraint(pi1));

		// function constraints for second program
		ProgramInstance pi2 = prog2.getInstance();
		pi2.init(ctx);
		solver.add(funcConstraint(pi2));

		// input - output constraint for two program for same inputs
		for (int i = 0; i < program.inputTypes.size(); i++) {
			solver.add(ctx.mkEq(pi1.inputVars.get(i), pi2.inputVars.get(i)));

			// only one termination character for a string.
			if (program.inputTypes.get(i).varType == VarType.ARRAY) {
				solver.add(StringUtil.wellFormedStringBound(ctx,
						(ArrayExpr) pi1.inputVars.get(i)));
			}
		}

		solver.add(ctx.mkNot(StringUtil.equal(ctx, program.outputType,
				pi1.outputVar, pi2.outputVar)));

		long start = System.currentTimeMillis();
		Status status = Z3Util.execute(ctx, solver, 1);
		long end = System.currentTimeMillis();

		System.out.println("Time spent on generating distinct program: "
				+ (end - start) + "ms");

		if (status == Status.SATISFIABLE) {
			printDistinctPrograms(solver, prog1, prog2);

			Model model = solver.getModel();

			Object[] newInputs = resolveInput(model, pi1);

			resolveOutputs(model, pi1, pi2);

			return newInputs;
		}
		return null;
	}
}