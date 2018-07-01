package thirdparty.CACheck.cellarray.synthesis;

import java.io.BufferedWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.apache.poi.ss.usermodel.Cell;

import thirdparty.CACheck.AMSheet;
import thirdparty.CACheck.CellArray;
import thirdparty.CACheck.R1C1Cell;
import thirdparty.CACheck.cellarray.inference.Constraints;
import thirdparty.CACheck.cellarray.inference.FormulaPattern;
import thirdparty.CACheck.formula.Function;
import thirdparty.CACheck.util.Log;
import thirdparty.CACheck.util.Utils;
import thirdparty.synthesis.basic.IOPair;
import thirdparty.synthesis.component.AverageComponent;
import thirdparty.synthesis.component.Component;
import thirdparty.synthesis.component.Components;
import thirdparty.synthesis.component.ConstantComponent;
import thirdparty.synthesis.component.MaxComponent;
import thirdparty.synthesis.component.MinComponent;
import thirdparty.synthesis.component.SumComponent;
import thirdparty.synthesis.util.Z3Util;

import com.microsoft.z3.ArithExpr;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.IntExpr;
import com.microsoft.z3.RealExpr;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Status;
import com.microsoft.z3.Z3Exception;

public class SynthesisUtils {

	public static boolean isSameSpec(List<Object> pattern1,
			List<Object> pattern2) {
		if (pattern1.size() != pattern2.size()) {
			return false;
		} else {
			int k = 0;
			for (; k < pattern1.size(); k++) {
				if (!pattern1.get(k).equals(pattern2.get(k))) {
					return false;
				}
			}
		}

		return true;
	}

	// use the formula and compare the semantics
	// We use the absolute addresses to compute the equalence. Because
	// sometimes, the different R1C1Cell may refer to the same cell.
	public static boolean semanticEqual(Cell cell, List<Object> pattern1,
			List<Object> pattern2) {
		// same spec, just return true
		if (isSameSpec(pattern1, pattern2)) {
			return true;
		}

		Set<R1C1Cell> input1 = FormulaPattern.getInputs(pattern1);
		Set<R1C1Cell> input2 = FormulaPattern.getInputs(pattern2);
		if (input1.size() != input2.size()) {
			return false;
		}

		Set<R1C1Cell> inputs = new HashSet<R1C1Cell>();
		inputs.addAll(input1);
		inputs.addAll(input2);

		// We don't consider the same cell with different R1C1 format.
		if (cell == null) {
			// input1 and input2 have different inputs
			if (inputs.size() > input1.size()) {
				return false;
			}
		}

		// they have the same output given the same input
		try {
			Context ctx = Z3Util.getContext();

			R1C1Cell[] ins = inputs.toArray(new R1C1Cell[0]);
			Map<R1C1Cell, RealExpr> cell2Real = new HashMap<R1C1Cell, RealExpr>();
			for (int i = 0; i < ins.length; i++) {
				cell2Real.put(ins[i], ctx.mkRealConst("x_" + i));
			}

			Solver solver = ctx.mkSolver();

			if (cell != null) {
				for (int i = 0; i < ins.length; i++) {
					for (int j = i + 1; j < ins.length; j++) {
						R1C1Cell in1 = ins[i];
						R1C1Cell in2 = ins[j];
						R1C1Cell in1_a1 = in1.getTrueCell(cell.getRowIndex(),
								cell.getColumnIndex());
						R1C1Cell in2_a1 = in2.getTrueCell(cell.getRowIndex(),
								cell.getColumnIndex());
						if (in1_a1.row == in2_a1.row
								&& in1_a1.column == in2_a1.column) {
							solver.add(ctx.mkEq(cell2Real.get(in1),
									cell2Real.get(in2)));
						}
					}
				}
			}

			ArithExpr expr1 = parsePatternReal(ctx, pattern1, cell2Real);
			ArithExpr expr2 = parsePatternReal(ctx, pattern2, cell2Real);
			BoolExpr noeq = ctx.mkNot(ctx.mkEq(expr1, expr2));

			solver.add(noeq);

			Status status = Z3Util.execute(ctx, solver, 1);
			if (status == Status.SATISFIABLE || status == Status.UNKNOWN) {
				return false;
			} else {
				return true;
			}
		} catch (Z3Exception e) {
			Log.logNewLine(e, Log.writer);
		}
		return true;
	}

	public static boolean semanticEqual(List<Object> pattern1,
			List<Object> pattern2) {
		return semanticEqual(null, pattern1, pattern2);
	}

	public static boolean compatible(Constraints cons, FormulaPattern fp1,
			FormulaPattern fp2) {

		// same spec, just return true
		if (isSameSpec(fp1.pattern, fp2.pattern)) {
			return true;
		}

		Set<R1C1Cell> allInputs = cons.getInputs();

		Set<R1C1Cell> inputs1 = FormulaPattern.getInputs(fp1.pattern);
		Set<R1C1Cell> inputs2 = FormulaPattern.getInputs(fp2.pattern);

		// they have the same input cells
		R1C1Cell[] inputs = allInputs.toArray(new R1C1Cell[0]);

		// they have the same output given the same input
		try {
			Context ctx = Z3Util.getContext();

			Map<R1C1Cell, RealExpr> cell2Real = new HashMap<R1C1Cell, RealExpr>();
			for (int i = 0; i < inputs.length; i++) {
				cell2Real.put(inputs[i], ctx.mkRealConst("x_" + i));
			}

			ArithExpr expr1 = SynthesisUtils.parsePatternReal(ctx, fp1.pattern,
					cell2Real);
			ArithExpr expr2 = SynthesisUtils.parsePatternReal(ctx, fp2.pattern,
					cell2Real);

			Solver solver = ctx.mkSolver();

			for (int i = 0; i < inputs.length; i++) {
				if (!inputs1.contains(inputs[i])
						&& !inputs2.contains(inputs[i])) {
					solver.add(ctx.mkEq(cell2Real.get(inputs[i]),
							ctx.mkReal("0")));
				} else {
					if (!inputs1.contains(inputs[i])) {
						// don't use zero. Using true values.
						Double dv = fp1.defaultValues.get(inputs[i]);
						if (dv != null) {
							solver.add(ctx.mkEq(cell2Real.get(inputs[i]),
									ctx.mkReal(dv.toString())));
						} else {
							solver.add(ctx.mkEq(cell2Real.get(inputs[i]),
									ctx.mkReal("0")));
						}
					}
					if (!inputs2.contains(inputs[i])) {
						// don't use zero. Using true values.
						Double dv = fp2.defaultValues.get(inputs[i]);
						if (dv != null) {
							solver.add(ctx.mkEq(cell2Real.get(inputs[i]),
									ctx.mkReal(dv.toString())));
						} else {
							solver.add(ctx.mkEq(cell2Real.get(inputs[i]),
									ctx.mkReal("0")));
						}
					}
				}
			}

			BoolExpr cond = ctx.mkNot(ctx.mkEq(expr1, expr2));
			solver.add(cond);

			Status status = Z3Util.execute(ctx, solver, 1);
			if (status == Status.SATISFIABLE) {
				return false;
			}
		} catch (Z3Exception e) {
			Log.logNewLine(e, Log.writer);
			return false;
		}

		return true;
	}

	public static boolean compatible(Constraints cons, List<Object> pattern1,
			List<Object> pattern2) {
		FormulaPattern fp1 = new FormulaPattern(pattern1);
		FormulaPattern fp2 = new FormulaPattern(pattern2);
		return compatible(cons, fp1, fp2);
	}

	public static ArithExpr parsePatternReal(Context ctx, List<Object> pattern,
			Map<R1C1Cell, RealExpr> cell2Var) throws Z3Exception {
		Stack<ArithExpr> stack = new Stack<ArithExpr>();
		for (Object o : pattern) {
			if (o instanceof R1C1Cell) {
				RealExpr input = cell2Var.get((R1C1Cell) o);
				stack.push(input);
			} else if (o instanceof Double) {
				stack.push(ctx.mkReal(o.toString()));
			} else if (o instanceof Function) {
				Function fun = (Function) o;
				ArithExpr result = fun.parseSpecReal(ctx, cell2Var);
				stack.push(result);
			} else {
				ArithExpr op2 = stack.pop();
				ArithExpr op1 = null;
				if (stack.isEmpty()) {
					op1 = ctx.mkInt(0);
				} else {
					op1 = stack.pop();
				}
				ArithExpr ret = getValue(ctx, ((String) o).charAt(0), op1, op2);
				stack.push(ret);
			}
		}

		return stack.pop();
	}

	public static ArithExpr parsePatternInt(Context ctx, List<Object> pattern,
			Map<R1C1Cell, IntExpr> inputs) throws Z3Exception {
		Stack<ArithExpr> stack = new Stack<ArithExpr>();
		for (Object o : pattern) {
			if (o instanceof R1C1Cell) {
				IntExpr input = (IntExpr) inputs.get((R1C1Cell) o);
				stack.push(input);
			} else if (o instanceof Double) {
				int tmp = ((Double) o).intValue();
				stack.push(ctx.mkInt(tmp));
			} else if (o instanceof Function) {
				Function fun = (Function) o;
				ArithExpr result = fun.parseSpecInt(ctx, inputs);
				stack.push(result);
			} else {
				ArithExpr op2 = stack.pop();
				ArithExpr op1 = null;
				if (stack.isEmpty()) {
					op1 = ctx.mkInt(0);
				} else {
					op1 = stack.pop();
				}
				ArithExpr ret = getValue(ctx, ((String) o).charAt(0), op1, op2);
				stack.push(ret);
			}
		}

		return stack.pop();
	}

	public static double checkPrecision(AMSheet sheet, CellArray ca) {

		if (ca.precision != null) {
			return ca.precision;
		}

		int[] stats = new int[7];
		for (int i = 0; i < ca.size(); i++) {
			Cell cell = ca.getCell(sheet, i);
			String type = cell.getCellStyle().getDataFormatString();
			int p = getPrecision(type);
			stats[p]++;
		}

		int dot = 0;
		double total = 0;
		for (; dot <= 5; dot++) {
			total += stats[dot];
			if (total / ca.size() >= 0.75) {
				break;
			}
		}
		double precison = 1 / Math.pow(10, dot) * 0.49999;
		ca.precision = precison;
		return ca.precision;
	}

	private static int getPrecision(String type) {
		int base = 0;
		if (type == null || "General".equals(type)) {
			return 5;
		}
		if (type.endsWith("%")) {
			base = 2;
			type = type.substring(0, type.length() - 1);
		}
		if (type.endsWith("_")) {
			type = type.substring(0, type.length() - 1);
		}
		if (type.endsWith("_ ")) {
			type = type.substring(0, type.length() - 2);
		}
		if (type.startsWith("# ##") || type.startsWith("#,##")) {
			type = type.substring(4, type.length());
		}
		if ("0".equals(type)) {
			return base;
		}
		if ("0.0".equals(type)) {
			return base + 1;
		}
		if ("0.00".equals(type)) {
			return base + 2;
		}
		if ("0.000".equals(type)) {
			return base + 3;
		}
		if ("0.0000".equals(type)) {
			return base + 4;
		}
		if ("0.00000".equals(type)) {
			return base + 5;
		}
		return 5;
	}

	public static boolean isError(AMSheet sheet, CellArray ca, Cell curCell,
			List<Object> pattern) {

		Double computeValue = computeValue(sheet, ca, curCell, pattern);
		Double value = 0.0;
		try {
			value = Utils.getNumericalValue(curCell);
		} catch (Exception e) {
		}

		double diff = computeValue - value;
		// TODO
		if (Math.abs(diff) < checkPrecision(sheet, ca)) {
			return false;
		} else {
			return true;
		}
	}

	public static double computeValue(AMSheet sheet, CellArray ca,
			Cell curCell, List<Object> pattern) {
		Stack<Double> stack = new Stack<Double>();
		for (Object o : pattern) {
			if (o instanceof R1C1Cell) {
				double data = 0;
				try {
					data = Utils.getNumericalValue(sheet, ca, (R1C1Cell) o,
							curCell);
				} catch (Exception e) {
				}
				stack.push(data);
			} else if (o instanceof Double) {
				stack.push((Double) o);
			} else if (o instanceof Function) {
				Function fun = (Function) o;
				double ret = fun.getFucntionValue(sheet, ca, curCell);
				stack.push(ret);
			} else {
				double op2 = stack.pop();
				double op1 = 0.0;
				if (stack.isEmpty()) {
					op1 = 0.0;
				} else {
					op1 = stack.pop();
				}
				double ret = getValue(((String) o).charAt(0), op1, op2);
				stack.push(ret);
			}
		}

		Double ret = stack.pop();

		return ret;
	}

	private static ArithExpr getValue(Context ctx, char op, ArithExpr op1,
			ArithExpr op2) throws Z3Exception {
		switch (op) {
		case '+':
			return ctx.mkAdd(op1, op2);
		case '-':
			return ctx.mkAdd(op1, ctx.mkUnaryMinus(op2));
		case '*':
			return ctx.mkMul(op1, op2);
		case '/':
			return (ArithExpr) ctx.mkITE(ctx.mkEq(op1, ctx.mkInt(0)),
					ctx.mkInt(0), ctx.mkDiv(op1, op2));
		default:
			return ctx.mkInt(0);
		}
	}

	private static double getValue(char op, double op1, double op2) {
		switch (op) {
		case '+':
			return op1 + op2;
		case '-':
			return op1 - op2;
		case '*':
			return op1 * op2;
		case '/':
			return op1 == 0.0 ? 0.0 : op1 / op2;
		default:
			return 0;
		}
	}

	public static void printConstraints(Constraints cons, BufferedWriter writer) {

		printFormulaPatterns(cons.getFormulaPatterns(), writer);

		printIOPairs(cons.getIOPairs(), writer);

		printComponents(cons.getComponents(), writer);
	}

	public static void printFormulaPatterns(List<FormulaPattern> fps,
			BufferedWriter writer) {
		Log.logNewLine("Formula patterns:", writer);
		for (FormulaPattern fp : fps) {
			Log.log(fp.toString(), writer);
			Log.logNewLine(writer);
		}
	}

	public static void printIOPairs(List<IOPair> pairs, BufferedWriter writer) {
		Log.logNewLine("Input-output pairs:", writer);
		for (IOPair pair : pairs) {
			for (Object in : pair.inputs) {
				Log.log(in + ", ", writer);
			}
			Log.log("--->" + pair.output, writer);
			Log.logNewLine(writer);
		}
	}

	public static void printComponents(List<Component> comps,
			BufferedWriter writer) {
		Log.log("Components: [", writer);
		for (Component comp : comps) {
			if (Components.CONSTANT == comp.getType()) {
				Log.log("CONSTANT(" + ((ConstantComponent) comp).defaultValue
						+ ")", writer);
			} else if (Components.PLUS == comp.getType()) {
				Log.log("PLUS", writer);
			} else if (Components.MINUS == comp.getType()) {
				Log.log("MINUS", writer);
			} else if (Components.MULT == comp.getType()) {
				Log.log("MULT", writer);
			} else if (Components.DIV == comp.getType()) {
				Log.log("DIV", writer);
			} else if (Components.SUM == comp.getType()) {
				Log.log("Sum(" + ((SumComponent) comp).getInputNum() + ")",
						writer);
			} else if (Components.AVERAGE == comp.getType()) {
				Log.log("Average(" + ((AverageComponent) comp).getInputNum()
						+ ")", writer);
			} else if (Components.MAX == comp.getType()) {
				Log.log("Max(" + ((MaxComponent) comp).getInputNum() + ")",
						writer);
			} else if (Components.MIN == comp.getType()) {
				Log.log("Min(" + ((MinComponent) comp).getInputNum() + ")",
						writer);
			}
			Log.log(", ", writer);
		}
		Log.log("]", writer);
		Log.logNewLine(writer);
	}
}
