package thirdparty.synthesis.state;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import thirdparty.synthesis.basic.Result;
import thirdparty.synthesis.basic.Type;
import thirdparty.synthesis.basic.VarType;
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

public class IOClassSynthesis extends BasicClassSynthesis {

	public void doSynthesis(ClassDescriptor cd, List<CallSequence> css)
			throws Z3Exception {

		ClassAbstract ca = cd.getClassAbstract();
		ca.init(ctx);

		Map<MethodAbstract, Result> rets = generateProgram(ca, css);

		if (rets == null) {
			System.out.println("No solution! Components insufficient!");
			return;
		} else {
			System.out.println("Current program:");
			Lval2Class.tranform(rets, ca);
		}

		System.out.println("Start to generate distinct classes...");
		generateDistinctClass(cd, css);
		System.out.println("Generating distinct classes has done.");
	}

	// generate a program which satisfies the input-output constraints.
	public Map<MethodAbstract, Result> generateProgram(ClassAbstract ca,
			List<CallSequence> css) throws Z3Exception {
		Solver solver = ctx.mkSolver();

		addSynthesisConstraints(solver, ca, css);

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
			List<CallSequence> css) throws Z3Exception {

		addWellFormConstraints(solver, ca);

		// initialize the ClassInstance.
		for (CallSequence cs : css) {
			ClassInstance ci = ca.getInstance(cs.calls);
			ci.init(ctx);

			// add function constraints
			solver.add(funcConstraints(ci));

			addInputOutputConstraints(solver, ci, cs.calls);
		}
	}

	public void addInputOutputConstraints(Solver solver, ClassInstance ci,
			List<Call> methodCalls) throws Z3Exception {
		for (int i = 0; i < ci.methodInstances.size(); i++) {
			MethodInstance mi = ci.methodInstances.get(i);
			Call call = methodCalls.get(i);

			addInputOutputConstraint(solver, mi, call);
		}
	}

	public void addInputOutputConstraint(Solver solver, MethodInstance mi,
			Call call) throws Z3Exception {

		solver.add(inputConstraint(mi, call.ioPair));

		solver.add(outputConstraint(mi, call.ioPair));
	}

	// generate distinct class
	public Object[] generateDistinctClass(ClassDescriptor cd,
			List<CallSequence> css) throws Z3Exception {
		Solver solver = ctx.mkSolver();

		// First class
		ClassAbstract clazz1 = cd.getClassAbstract();
		clazz1.init(ctx);
		addSynthesisConstraints(solver, clazz1, css);

		// Second class
		ClassAbstract clazz2 = cd.getClassAbstract();
		clazz2.init(ctx);
		addSynthesisConstraints(solver, clazz2, css);

		List<int[]> allSeqs = allCallSequences(cd.getMethods().size() - 1, 3);
		Map<CallSequence, ClassInstancePair> ciMap = new HashMap<CallSequence, ClassInstancePair>();

		// (1) use existing call sequence.
		// solver.add(callSequenceConstrain(cd, clazz1, clazz2, css.get(0),
		// ciMap));

		IntExpr seqNo = ctx.mkIntConst("seqNo");
		List<CallSequence> newCss = new ArrayList<CallSequence>();
		BoolExpr newCssCons = ctx.mkBool(false);
		for (int i = 0; i < allSeqs.size(); i++) {
			int[] seq = allSeqs.get(i);
			CallSequence cs = new CallSequence();
			for (int c : seq) {
				Call call = new Call(cd.getMethods().get(c).methodName, null);
				cs.addCall(call);
			}
			newCss.add(cs);

			BoolExpr newCons = ctx.mkAnd(ctx.mkEq(seqNo, ctx.mkInt(i)),
					callSequenceConstrain(cd, clazz1, clazz2, cs, ciMap));

			newCssCons = ctx.mkOr(newCssCons, newCons);
		}

		solver.add(newCssCons);

		long start = System.currentTimeMillis();
		Status status = Z3Util.execute(ctx, solver, 60);
		long end = System.currentTimeMillis();

		System.out.println("Time spent on generating distinct class: "
				+ (end - start) + "ms");

		if (status == Status.SATISFIABLE) {
			printDistinctClasses(solver, clazz1, clazz2);

			Model model = solver.getModel();

			int index = (Integer) Z3Util.getValue(ctx, model, VarType.INTEGER,
					seqNo);
			CallSequence cs = newCss.get(index);

			ClassInstancePair cip = ciMap.get(cs);
			for (int i = 0; i < cip.ci1.methodInstances.size(); i++) {
				MethodInstance mi1 = cip.ci1.methodInstances.get(i);
				MethodInstance mi2 = cip.ci2.methodInstances.get(i);
				Object[] newInputs = resolveInput(model, mi1);
				System.out.println(mi1.getAbstract().methodName + ": "
						+ Z3Util.printObjects(newInputs));

				resolveOutputs(model, mi1, mi2);

				resolveStates(model, mi1, mi2);
			}
		}
		return null;
	}

	// generate distinct class
	private BoolExpr callSequenceConstrain(ClassDescriptor cd,
			ClassAbstract clazz1, ClassAbstract clazz2, CallSequence cs,
			Map<CallSequence, ClassInstancePair> ciMap) throws Z3Exception {

		BoolExpr ret = ctx.mkBool(true);

		// function constraints for first class
		ClassInstance ci1 = clazz1.getInstance(cs.calls);
		ci1.init(ctx);
		ret = ctx.mkAnd(ret, funcConstraints(ci1));

		// function constraints for second program
		ClassInstance ci2 = clazz2.getInstance(cs.calls);
		ci2.init(ctx);
		ret = ctx.mkAnd(ret, funcConstraints(ci2));

		ciMap.put(cs, new ClassInstancePair(ci1, ci2));

		// input - output constraint for two program for same inputs
		for (int i = 0; i < cs.calls.size(); i++) {
			MethodInstance mi1 = ci1.methodInstances.get(i);
			MethodInstance mi2 = ci2.methodInstances.get(i);

			for (int j = 0; j < mi1.getAbstract().inputTypes.size(); j++) {
				ret = ctx.mkAnd(ret,
						ctx.mkEq(mi1.inputVars.get(j), mi2.inputVars.get(j)));

				// only one termination character for a string.
				if (mi1.getAbstract().inputTypes.get(j).varType == VarType.ARRAY) {
					ret = ctx.mkAnd(ret, StringUtil.wellFormedStringBound(ctx,
							(ArrayExpr) mi1.inputVars.get(j)));
				}
			}
		}

		// output is not equal
		BoolExpr neq = ctx.mkBool(false);
		for (int i = 0; i < cs.calls.size(); i++) {
			MethodInstance mi1 = ci1.methodInstances.get(i);
			MethodInstance mi2 = ci2.methodInstances.get(i);

			BoolExpr eq = StringUtil.equal(ctx, mi1.getAbstract().outputType,
					mi1.outputVar, mi2.outputVar);

			neq = ctx.mkOr(neq, ctx.mkNot(eq));
		}
		// states are not equal.
		/*
		 * TODO Even the states are not equal, but the methods can't expose the
		 * differences, so we can't get useful inputs for the method call
		 * sequence. We at least add a new method to expose the difference.
		 */
		for (int i = 0; i < cs.calls.size(); i++) {
			List<Expr> states1 = ci1.methodInstances.get(i).outStateVars;
			List<Expr> states2 = ci2.methodInstances.get(i).outStateVars;

			List<Type> stateTypes = ci1.calssAbstract.stateAbstract.stateTypes;
			for (int j = 0; j < stateTypes.size(); j++) {
				BoolExpr eq = StringUtil.equal(ctx, stateTypes.get(j),
						states1.get(j), states2.get(j));

				neq = ctx.mkOr(neq, ctx.mkNot(eq));
			}
		}

		ret = ctx.mkAnd(ret, neq);

		return ret;
	}

	public List<int[]> allCallSequences(int methodSize, int maxLength) {
		List<int[]> allSeqs = new ArrayList<int[]>();

		List<Integer> curSeq = new ArrayList<Integer>();
		for (int i = 0; i < maxLength; i++) {
			curSeq.add(i);
		}
		nexCallSequence(curSeq, methodSize, maxLength, maxLength, allSeqs);

		return allSeqs;

	}

	private void nexCallSequence(List<Integer> curSeq, int methodSize,
			int maxLength, int length, List<int[]> allSeqs) {
		if (length == 0) {
			for (int i = 1; i <= methodSize; i++) {
				if (!curSeq.contains(i)) {
					return;
				}
			}
			int[] seq = new int[maxLength];
			for (int i = 0; i < maxLength; i++) {
				seq[i] = curSeq.get(i);
			}
			allSeqs.add(seq);
		} else {
			for (int i = 1; i <= methodSize; i++) {
				curSeq.set(maxLength - length, i);
				nexCallSequence(curSeq, methodSize, maxLength, length - 1,
						allSeqs);
			}
		}
	}
}