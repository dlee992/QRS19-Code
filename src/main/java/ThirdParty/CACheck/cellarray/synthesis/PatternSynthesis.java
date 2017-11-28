package ThirdParty.CACheck.cellarray.synthesis;

import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ThirdParty.CACheck.AMSheet;
import ThirdParty.CACheck.CellArray;
import ThirdParty.CACheck.R1C1Cell;
import ThirdParty.CACheck.amcheck.AnalysisPattern;
import ThirdParty.CACheck.cellarray.extract.CAResult;
import ThirdParty.CACheck.cellarray.inference.ConstraintGenerator;
import ThirdParty.CACheck.cellarray.inference.Constraints;
import ThirdParty.CACheck.cellarray.inference.Coverage;
import ThirdParty.CACheck.cellarray.inference.FormulaPattern;
import ThirdParty.CACheck.cellarray.inference.Lval2Formula;
import ThirdParty.CACheck.cellarray.inference.SimpleRecover;
import ThirdParty.CACheck.formula.ParseFormula;
import ThirdParty.CACheck.util.Log;
import ThirdParty.synthesis.basic.IOPair;
import ThirdParty.synthesis.basic.ProgramAbstract;
import ThirdParty.synthesis.basic.ProgramInstance;
import ThirdParty.synthesis.basic.Result;
import ThirdParty.synthesis.basic.Type;
import ThirdParty.synthesis.component.Component;
import ThirdParty.synthesis.io.IOSynthesis;
import ThirdParty.synthesis.util.Z3Util;

import com.microsoft.z3.ArithExpr;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.IntExpr;
import com.microsoft.z3.IntNum;
import com.microsoft.z3.Model;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Status;
import com.microsoft.z3.Z3Exception;

public class PatternSynthesis extends IOSynthesis {

	private AMSheet sheet = null;

	public PatternSynthesis(AMSheet sheet) {
		this.sheet = sheet;
	}

	public List<Object> doSynthesis(CAResult car, BufferedWriter writer) {
		List<Object> pattern = null;
		if (AnalysisPattern.cached) {
			pattern = PatternCache.getCachedPattern(car);

			if (pattern == null) {
				pattern = doInternalSynthesis(car, writer);
				if (pattern != null) {
					PatternCache.cachePattern(car, pattern);
				} else {
					// add a mock result.
					List<Object> mock = new ArrayList<Object>();
					PatternCache.cachePattern(car, mock);
				}
			} else if (pattern.size() == 0) {
				// empty list for mock result.
				pattern = null;
			}
		} else {
			pattern = doInternalSynthesis(car, writer);
		}

		return pattern;
	}

	public List<Object> doInternalSynthesis(CAResult car, BufferedWriter writer) {
		Constraints cons = car.constraints;

		// print all the constraints
		SynthesisUtils.printConstraints(cons, writer);

		List<Object> curPattern = null;
		try {
			float maxCoverage = 0.0f;
			float maxCECoverage = 0.0f;
			for (List<FormulaPattern> group : SpecClassify.classify(cons)) {
				// simplify the group when some formula patterns only have
				// different constants
				group = ConstraintGenerator.simplifyFormulaPatterns(group);

				// do simple recover again.
				SimpleRecover sr = new SimpleRecover(sheet);
				List<Object> pattern = sr.doGroupRecover(car, group, writer);
				// program ThirdParty.synthesis.
				if (pattern == null) {
					// TODO filter the CAs which we can't solve. Only for
					// performance, we can delete this.
					if (cons.getInputs().size() <= 10
							&& cons.getComponents().size() <= 10) {
						List<IOPair> ioPairs = doPatternSynthesis(
								cons.getInputs(), cons.getComponents(), group,
								writer);
						pattern = doIOSynthesis(car, writer, ioPairs);
					}
				}

				float coverage = 0.0f;
				float ceCoverage = 0.0f;
				if (pattern != null) {
					Log.logNewLine("*****Synthesis succeed!", writer);
					coverage = new Coverage(sheet)
							.computeCoverage(pattern, car);
					ceCoverage = new Coverage(sheet).computeCECoverage(pattern,
							car);
				} else {
					Log.logNewLine("*****Can't synthesize a pattern!", writer);
				}

				if (coverage > maxCoverage || coverage == maxCoverage
						&& ceCoverage > maxCECoverage) {
					maxCoverage = coverage;
					maxCECoverage = ceCoverage;
					curPattern = pattern;
				}
			}
		} catch (Exception e) {
			Log.logNewLine(e, writer);
		}

		return curPattern;
	}

	public List<Object> doIOSynthesis(CAResult car, BufferedWriter writer,
			List<IOPair> ioPairs) {
		Constraints cons = car.constraints;
		CellArray ca = car.cellArray;

		Log.logNewLine("IO Synthesize starting....", writer);

		try {
			List<Type> inputTypes = new ArrayList<Type>();
			for (int i = 0; i < cons.getInputs().size(); i++) {
				inputTypes.add(Type.intType());
			}

			ProgramAbstract program = new ProgramAbstract(cons.getComponents(),
					inputTypes, Type.intType());
			program.init(ctx);

			List<IOPair> curPairs = ioPairs;
			Result curRes = null;
			List<IOPair> pairs = cons.getIOPairs();

			int i = 0;
			do {
				SynthesisUtils.printIOPairs(curPairs, writer);

				long start = System.currentTimeMillis();
				Result res = generateProgram(program, curPairs);
				long end = System.currentTimeMillis();
				Log.logNewLine("Time spent on generating program: "
						+ (end - start) + "ms", writer);

				if (res != null) {
					curRes = res;
					String formula = Lval2Formula.tranform(ca, res, program,
							cons.getInputs(), false);
					Log.logNewLine("Current pattern: " + formula, writer);

					start = System.currentTimeMillis();
					List<Integer> newInputs = generateDistinctProgram(program,
							curPairs, cons.getInputs(),
							cons.getFormulaPatterns(), false);
					end = System.currentTimeMillis();
					Log.logNewLine(
							"Time spent on generating distinct program: "
									+ (end - start) + "ms", writer);

					if (newInputs == null) {
						break;
					}
				} else {
					if (!curPairs.isEmpty()) {
						curPairs.remove(curPairs.size() - 1);
					}
				}

				curPairs.add(pairs.get(i));
				i++;
			} while (i < pairs.size() && i < 5);

			String formula = null;
			List<Object> pattern = null;
			if (curRes != null) {
				formula = Lval2Formula.tranform(ca, curRes, program,
						cons.getInputs(), true);
				int row = 0;
				int col = 0;
				if (ca.isRowCA) {
					row = ca.rowOrColumn;
					col = ca.start;
				} else {
					row = ca.start;
					col = ca.rowOrColumn;
				}
				pattern = ParseFormula.parse(ca, row, col, formula);
			} else {
				Log.logNewLine("No Formula!", writer);
			}

			Log.logNewLine("IO Synthesize ending....", writer);

			return pattern;
		} catch (Z3Exception e) {
			Log.log(e.toString(), writer);
		}

		return null;
	}

	public List<IOPair> doPatternSynthesis(Set<R1C1Cell> inputs,
			List<Component> comps, List<FormulaPattern> fps,
			BufferedWriter writer) throws Z3Exception {

		List<Type> inputTypes = new ArrayList<Type>();
		for (int i = 0; i < inputs.size(); i++) {
			inputTypes.add(Type.intType());
		}

		ProgramAbstract program = new ProgramAbstract(comps, inputTypes,
				Type.intType());
		program.init(ctx);

		List<IOPair> ioPairs = new ArrayList<IOPair>();
		IOPair iop = generateIOPair(inputs, fps, null);
		if (iop != null) {
			ioPairs.add(iop);
		}

		Log.logNewLine(writer);
		Log.logNewLine("Pattern Synthesize starting....", writer);
		SynthesisUtils.printFormulaPatterns(fps, writer);
		SynthesisUtils.printComponents(comps, writer);
		while (true) {
			SynthesisUtils.printIOPairs(ioPairs, writer);

			long start = System.currentTimeMillis();
			Result res = generateProgram(program, ioPairs);
			long end = System.currentTimeMillis();
			Log.logNewLine("Time spent on generating program: " + (end - start)
					+ "ms", writer);

			if (res != null) {
				String formula = Lval2Formula.tranform(null, res, program,
						inputs, false);
				Log.logNewLine("Current pattern: " + formula, writer);

				start = System.currentTimeMillis();
				List<Integer> newInputs = generateDistinctProgram(program,
						ioPairs, inputs, fps, true);
				end = System.currentTimeMillis();
				Log.logNewLine("Time spent on generating distinct program: "
						+ (end - start) + "ms", writer);

				if (newInputs != null) {
					ioPairs.add(generateIOPair(inputs, fps, newInputs));
				} else {
					break;
				}
			} else {
				break;
			}
		}

		Log.logNewLine("Pattern Synthesize ending....", writer);

		return ioPairs;
	}

	// generate distinct program
	public List<Integer> generateDistinctProgram(ProgramAbstract program,
			List<IOPair> ioPairs, Set<R1C1Cell> inputs,
			List<FormulaPattern> fps, boolean inputConstraints)
			throws Z3Exception {
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

		// input - output constraint for two program for different inputs
		for (int i = 0; i < program.inputTypes.size(); i++) {
			solver.add(ctx.mkEq(pi1.inputVars.get(i), pi2.inputVars.get(i)));
		}

		if (inputConstraints) {
			BoolExpr inputCons = getInputConstraints(ctx, pi1.inputVars,
					inputs, fps);
			solver.add(inputCons);
		}

		solver.add(ctx.mkNot(ctx.mkEq(pi1.outputVar, pi2.outputVar)));

		long start = System.currentTimeMillis();
		Status status = Z3Util.execute(ctx, solver, 1);
		long end = System.currentTimeMillis();

		System.out.println("Time spent on generating distinct program: "
				+ (end - start) + "ms");

		if (status == Status.SATISFIABLE) {
			Model model = solver.getModel();

			List<Integer> newInputs = new ArrayList<Integer>();
			for (int i = 0; i < program.inputTypes.size(); i++) {
				int input = ((IntNum) model
						.getConstInterp(pi1.inputVars.get(i))).getInt();
				newInputs.add(input);
			}
			return newInputs;
		}
		return null;
	}

	public BoolExpr getInputConstraints(Context context, List<Expr> inputVars,
			Set<R1C1Cell> inputs, List<FormulaPattern> specs)
			throws Z3Exception {
		BoolExpr res = context.mkBool(false);
		for (FormulaPattern spec : specs) {
			BoolExpr specRes = getPatternInputConstraints(context, inputVars,
					inputs, spec.pattern);
			res = context.mkOr(res, specRes);
		}
		return res;
	}

	public BoolExpr getPatternInputConstraints(Context context,
			List<Expr> inputVars, Set<R1C1Cell> inputs, List<Object> pattern)
			throws Z3Exception {

		R1C1Cell[] allInputs = inputs.toArray(new R1C1Cell[0]);

		Set<R1C1Cell> patternInputs = FormulaPattern.getInputs(pattern);
		BoolExpr res = context.mkBool(true);
		for (int i = 0; i < allInputs.length; i++) {
			if (!patternInputs.contains(allInputs[i])) {
				res = context.mkAnd(res,
						context.mkEq(inputVars.get(i), context.mkInt(0)));
			}
		}
		return res;
	}

	public IOPair generateIOPair(Set<R1C1Cell> inputs,
			List<FormulaPattern> specs, List<Integer> concreteInputs)
			throws Z3Exception {
		List<Object> pattern = selectPattern(inputs, specs, concreteInputs);

		R1C1Cell[] allInputs = inputs.toArray(new R1C1Cell[0]);

		Context context = Z3Util.getContext();
		Solver solver = context.mkSolver();

		Map<R1C1Cell, IntExpr> cell2Int = new HashMap<R1C1Cell, IntExpr>();
		List<Expr> allVars = new ArrayList<Expr>();
		for (int i = 0; i < allInputs.length; i++) {
			IntExpr var = context.mkIntConst("x_" + Z3Util.getVarName());
			cell2Int.put(allInputs[i], var);
			allVars.add(var);
		}

		ArithExpr expr = SynthesisUtils.parsePatternInt(context, pattern,
				cell2Int);

		if (concreteInputs == null) {
			BoolExpr inputCons = getPatternInputConstraints(context, allVars,
					inputs, pattern);
			solver.add(inputCons);
		} else {
			for (int i = 0; i < allInputs.length; i++) {
				solver.add(context.mkEq(allVars.get(i),
						context.mkInt(concreteInputs.get(i))));
			}
		}

		IntExpr resExpr = context.mkIntConst("res_" + Z3Util.getVarName());

		BoolExpr cond = context.mkEq(expr, resExpr);
		solver.add(cond);

		Status status = Z3Util.execute(context, solver, 1);
		if (status == Status.SATISFIABLE) {
			Model model = solver.getModel();

			Integer[] newInputs = new Integer[inputs.size()];
			for (int i = 0; i < newInputs.length; i++) {
				int input = ((IntNum) model.getConstInterp(allVars.get(i)))
						.getInt();
				newInputs[i] = input;
			}
			int output = ((IntNum) model.getConstInterp(resExpr)).getInt();

			return new IOPair(newInputs, output);
		}
		return null;
	}

	public static List<Object> selectPattern(Set<R1C1Cell> inputs,
			List<FormulaPattern> fps, List<Integer> concreteInputs) {
		if (concreteInputs == null) {
			return fps.get(0).pattern;
		}

		R1C1Cell[] allInputs = inputs.toArray(new R1C1Cell[0]);

		for (FormulaPattern fp : fps) {
			Set<R1C1Cell> patternInputs = fp.getInputs();
			boolean isComputed = true;
			for (int i = 0; i < allInputs.length; i++) {
				if (!patternInputs.contains(allInputs[i])) {
					if (concreteInputs.get(i) != 0) {
						isComputed = false;
						break;
					}
				}
			}
			if (isComputed) {
				return fp.pattern;
			}
		}
		return null;
	}
}