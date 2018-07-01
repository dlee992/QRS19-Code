package thirdparty.synthesis.basic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import thirdparty.synthesis.component.Component;
import thirdparty.synthesis.component.Components;
import thirdparty.synthesis.util.StringUtil;
import thirdparty.synthesis.util.Z3Util;

import com.microsoft.z3.ArithExpr;
import com.microsoft.z3.ArrayExpr;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.IntExpr;
import com.microsoft.z3.Model;
import com.microsoft.z3.RealExpr;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Z3Exception;

public class BasicSynthesis {

	public Context ctx = Z3Util.getContext();

	// well formedness constrains
	public void addWellFormConstraint(Solver solver, ProgramAbstract program)
			throws Z3Exception {
		List<IntExpr> locs = program.locVars;
		List<Type> types = program.types;

		int psize = program.getProgramSize();
		int inputIndex = 0;
		for (int i = 0; i < locs.size(); i++) {
			IntExpr loc = locs.get(i);
			Type type = types.get(i);
			if (type.ioType == IOType.STATE_GET) {
				solver.add(ctx.mkEq(loc, ctx.mkInt(inputIndex++)));
			} else if (type.ioType == IOType.FUN_INPUT) {
				solver.add(ctx.mkEq(loc, ctx.mkInt(inputIndex++)));
			} else if (type.ioType == IOType.COMP_INPUT) {
				solver.add(ctx.mkGe(loc, ctx.mkInt(0)),
						ctx.mkLt(loc, ctx.mkInt(psize)));
			} else if (type.ioType == IOType.COMP_OUTPUT) {
				solver.add(ctx.mkGe(loc, ctx.mkInt(program.getInputSize())),
						ctx.mkLt(loc, ctx.mkInt(psize)));
			} else if (type.ioType == IOType.STATE_SET) {
				solver.add(ctx.mkGe(loc, ctx.mkInt(0)),
						ctx.mkLt(loc, ctx.mkInt(psize)));
			} else if (type.ioType == IOType.FUN_OUTPUT) {
				solver.add(ctx.mkGe(loc, ctx.mkInt(0)),
						ctx.mkLt(loc, ctx.mkInt(psize)));
			}
		}

		// constraints for one component in each line
		for (int i = 0; i < locs.size(); i++) {
			if (types.get(i).ioType == IOType.COMP_OUTPUT) {
				IntExpr iLoc = locs.get(i);
				for (int j = i + 1; j < locs.size(); j++) {
					if (types.get(j).ioType == IOType.COMP_OUTPUT) {
						IntExpr jLoc = locs.get(j);
						solver.add(ctx.mkNot(ctx.mkEq(iLoc, jLoc)));
					}
				}
			}
		}

		// constraints for inputs of each component are defined before used
		for (int i = 0; i < locs.size(); i++) {
			if (types.get(i).ioType != IOType.COMP_INPUT) {
				continue;
			}

			int j = i + 1;
			for (; j < locs.size(); j++) {
				if (types.get(j).ioType == IOType.COMP_OUTPUT) {
					break;
				}
			}
			for (int k = i; k < j; k++) {
				solver.add(ctx.mkLt(locs.get(k), locs.get(j)));
			}

			// compute the next component
			i = j;
		}

		// constraints for switchable components
		int compIndex = 0;
		for (int i = 0; i < locs.size(); i++) {
			if (types.get(i).ioType != IOType.COMP_OUTPUT) {
				continue;
			}

			Component comp = program.components.get(compIndex);
			if (comp.switchable) {
				int j = i - 1;
				for (;; j--) {
					if (types.get(j).ioType != IOType.COMP_INPUT) {
						break;
					}
				}
				for (int k = j + 1; k < i - 1; k++) {
					// solver.add(ctx.mkLt(locs.get(k), locs.get(k + 1)));
				}
			}

			compIndex++;
		}

		// constraints for same components
		int firstCompIndex = 0;
		for (int i = 0; i < locs.size(); i++) {
			if (types.get(i).ioType != IOType.COMP_OUTPUT) {
				continue;
			}

			Component firstComp = program.components.get(firstCompIndex);

			int secondCompIndex = firstCompIndex + 1;
			for (int j = i + 1; j < locs.size(); j++) {
				if (types.get(j).ioType != IOType.COMP_OUTPUT) {
					continue;
				}
				Component secondComp = program.components.get(secondCompIndex);

				if (firstComp.type == secondComp.type) {
					// solver.add(ctx.mkLt(locs.get(i), locs.get(j)));
				}

				secondCompIndex++;
			}

			firstCompIndex++;
		}
	}

	// function constraints
	public BoolExpr funcConstraint(ProgramInstance pi) throws Z3Exception {

		List<IntExpr> locs = pi.getAbstract().locVars;
		List<Expr> allVars = pi.allVars;

		BoolExpr ret = ctx.mkBool(true);
		// constants have the same values
		int constId = 0;
		for (Component comp : pi.components) {
			if (comp.getType() == Components.CONSTANT) {
				Expr compValue = comp.getVariables().get(0);
				Expr consValue = pi.getAbstract().constVars.get(constId);
				ret = ctx.mkAnd(ret, ctx.mkEq(compValue, consValue));
				constId++;
			}
		}

		// the relation between location variables and program variables
		for (int i = 0; i < allVars.size(); i++) {
			for (int j = i + 1; j < allVars.size(); j++) {
				BoolExpr c = null;
				if (pi.getAbstract().types.get(i).varType != pi.getAbstract().types
						.get(j).varType) {
					// the type constraints
					c = ctx.mkNot(ctx.mkEq(locs.get(i), locs.get(j)));
				} else {
					// data flow constraints
					c = ctx.mkImplies(ctx.mkEq(locs.get(i), locs.get(j)),
							ctx.mkEq(allVars.get(i), allVars.get(j)));
				}
				ret = ctx.mkAnd(ret, c);
			}
		}

		// the specifications for the components
		for (Component comp : pi.components) {
			ret = ctx.mkAnd(ret, comp.getSpecification());
		}

		return ret;
	}

	// transform the result from solver to int array for location variables
	public Result resolveResult(Solver solver, ProgramAbstract program)
			throws Z3Exception {

		Model model = solver.getModel();

		List<Integer> ls = new ArrayList<Integer>();

		for (IntExpr loc : program.locVars) {
			int l = (Integer) Z3Util.getValue(ctx, model, VarType.INTEGER, loc);
			ls.add(l);
		}

		List<Object> cs = new ArrayList<Object>();

		int index = 0;
		for (Component comp : program.components) {
			if (comp.getType() == Components.CONSTANT) {
				Expr var = program.constVars.get(index);
				Object value = Z3Util.getValue(ctx, model, comp.varType, var);
				cs.add(value);
				index++;
			}
		}

		return new Result(ls, cs);
	}

	public Object[] resolveInput(Model model, ProgramInstance pi)
			throws Z3Exception {

		List<Object> ret = new ArrayList<Object>();

		for (int i = 0; i < pi.getAbstract().inputTypes.size(); i++) {
			Object value = Z3Util.getValue(ctx, model,
					pi.getAbstract().inputTypes.get(i).varType,
					pi.inputVars.get(i));
			ret.add(value);
		}
		return ret.toArray();
	}

	@SuppressWarnings("unchecked")
	public void resolveOutputs(Model model, ProgramInstance pi1,
			ProgramInstance pi2) throws Z3Exception {

		Type outputType = pi1.getAbstract().outputType;
		if (pi1.getAbstract().isReturnVoid()) {
			return;
		}

		Object output1 = Z3Util.getValue(ctx, model, outputType.varType,
				pi1.outputVar);
		Object output2 = Z3Util.getValue(ctx, model, outputType.varType,
				pi2.outputVar);

		if (outputType.varType == VarType.INTEGER
				|| outputType.varType == VarType.BOOLEAN
				|| outputType.varType == VarType.DOUBLE) {
			System.out.println("  --Output 1:" + output1);
			System.out.println("  --Output 2:" + output2);
		} else if (outputType.varType == VarType.ARRAY) {
			System.out.println("  --Output 1:"
					+ Z3Util.printString((int[]) output1));
			System.out.println("  --Output 2:"
					+ Z3Util.printString((int[]) output2));
		}
		if (outputType.varType == VarType.HASH) {
			System.out.println("  --Output 1:"
					+ Z3Util.printMap((Map<Integer, int[]>) output1));
			System.out.println("  --Output 2:"
					+ Z3Util.printMap((Map<Integer, int[]>) output2));
		}
	}

	public BoolExpr inputConstraint(ProgramInstance pi, IOPair pair)
			throws Z3Exception {
		BoolExpr cons = ctx.mkBool(true);
		for (int i = 0; i < pi.inputVars.size(); i++) {
			Object input = pair.inputs[i];
			BoolExpr tmp = equalConstraint(pi.inputVars.get(i), input,
					pi.getAbstract().inputTypes.get(i).varType);
			cons = ctx.mkAnd(cons, tmp);
		}

		return cons;
	}

	public BoolExpr outputConstraint(ProgramInstance pi, IOPair pair)
			throws Z3Exception {
		if (pi.getAbstract().isReturnVoid()) {
			return ctx.mkBool(true);
		}

		Object output = pair.output;

		if (pi.getAbstract().outputType.varType == VarType.DOUBLE) {
			double o = new Double(output.toString());
			ArithExpr outputVar = (ArithExpr) pi.outputVar;

			double delta = 0.01;
			RealExpr low = ctx.mkReal(new Double(o - delta).toString());
			RealExpr high = ctx.mkReal(new Double(o + delta).toString());

			BoolExpr cons = ctx.mkAnd(ctx.mkGt(outputVar, low),
					ctx.mkLt(outputVar, high));
			
			return cons;
		}

		BoolExpr cons = equalConstraint(pi.outputVar, output,
				pi.getAbstract().outputType.varType);
		return cons;
	}

	protected BoolExpr equalConstraint(Expr var, Object input, VarType vt)
			throws Z3Exception {
		if (vt == VarType.INTEGER) {
			BoolExpr inputCons = ctx.mkEq(var, ctx.mkInt((Integer) input));
			return inputCons;
		} else if (vt == VarType.DOUBLE) {
			BoolExpr inputCons = ctx.mkEq(var, ctx.mkReal(input.toString()));
			return inputCons;
		} else if (vt == VarType.BOOLEAN) {
			BoolExpr inputCons = ctx.mkEq(var, ctx.mkBool((Boolean) input));
			return inputCons;
		} else if (vt == VarType.ARRAY) {
			BoolExpr inputCons = ctx.mkBool(true);
			int[] arrInput = (int[]) input;
			ArrayExpr varInput = (ArrayExpr) var;
			for (int j = 0; j < arrInput.length; j++) {
				BoolExpr arrInputCons = ctx.mkEq(
						ctx.mkSelect(varInput, ctx.mkInt(j)),
						ctx.mkInt(arrInput[j]));
				inputCons = ctx.mkAnd(inputCons, arrInputCons);
			}

			// change other elements to 0
			BoolExpr fillCons = StringUtil.fillArray0Bound(ctx, varInput,
					arrInput.length);

			return ctx.mkAnd(inputCons, fillCons);
		}
		return ctx.mkBool(true);
	}

	public void printDistinctPrograms(Solver solver, ProgramAbstract prog1,
			ProgramAbstract prog2) throws Z3Exception {

		System.out.println("Distinct program 1:");
		Result res1 = resolveResult(solver, prog1);
		Lval2Prog.tranform(res1, prog1, true);

		System.out.println("Distinct program 2:");
		Result res2 = resolveResult(solver, prog2);
		Lval2Prog.tranform(res2, prog2, true);
	}

	public void printIOPairs(List<IOPair> ioPairs) {
		System.out.println("input-output pairs:");
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < ioPairs.size(); i++) {
			IOPair pair = ioPairs.get(i);
			sb.append(pair);

			if (i < ioPairs.size() - 1) {
				sb.append(", ");
			}
		}
		System.out.println(sb);
	}
}