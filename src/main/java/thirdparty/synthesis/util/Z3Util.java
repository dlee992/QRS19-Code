package thirdparty.synthesis.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import thirdparty.synthesis.basic.VarType;

import com.microsoft.z3.ArrayExpr;
import com.microsoft.z3.ArraySort;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.IntNum;
import com.microsoft.z3.Model;
import com.microsoft.z3.Params;
import com.microsoft.z3.RatNum;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Status;
import com.microsoft.z3.Z3Exception;

public class Z3Util {

	public static int id = 0;

	public static String getVarName() {
		return "temp_" + id++;
	}

	public static Context getContext() {
		HashMap<String, String> cfg = new HashMap<String, String>();
		cfg.put("model", "true");
		// global z3 timeout.
		// cfg.put("timeout", "10000");
		try {
			return new Context(cfg);
		} catch (Z3Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static Status execute(final Context ctx, final Solver solver,
			int time) throws Z3Exception {
		return execute(ctx, solver, time, true);
	}

	public static Status execute(final Context ctx, final Solver solver,
			int time, boolean disposed) throws Z3Exception {
		ctx.updateParamValue("timeout", "" + time * 1000 * 60);
		// ctx.updateParamValue("memory_max_size", "" + 10 * 1024); // 10G

		Params params = ctx.mkParams();
		params.add("soft_timeout", time * 1000 * 60);
		params.add("max_memory", 10 * 1024); // 10G
		solver.setParameters(params);

		ExecutorService executor = Executors.newFixedThreadPool(1);
		FutureTask<Status> task = new FutureTask<Status>(
				new Callable<Status>() {
					public Status call() throws Exception {
						return solver.check();
					}
				});
		executor.execute(task);
		executor.shutdown();
		Status status = null;
		try {
			status = task.get(time + 1, TimeUnit.MINUTES);
		} catch (Exception e) {
			System.err.println("Slover failed: " + e.getMessage());
			status = Status.UNKNOWN;
		}

		// remove the memory when we can't get results.
		if (status != Status.SATISFIABLE && disposed) {
			try {
				//todo
				solver.dispose();
			} catch (Exception e) {
			}
		}

		return status;
	}

	static ExecutorService executor = Executors.newCachedThreadPool();

	public static Status execute1(final Context ctx, final Solver solver,
			final int time) {

		FutureTask<Status> task = new FutureTask<Status>(
				new Callable<Status>() {
//					@Override
					public Status call() throws Exception {
						Status status = execute1(ctx, solver, time);
						return status;
					}
				});

		executor.execute(task);

		try {
			return task.get(time + 1, TimeUnit.MINUTES);
		} catch (Exception e) {
			System.err.println("Slover failed: " + e.getMessage());
		}

		return Status.UNKNOWN;
	}

	public static Expr getVariable(VarType varType, Context ctx, String varName)
			throws Z3Exception {
		if (varType == VarType.INTEGER) {
			return ctx.mkIntConst(varName);
		} else if (varType == VarType.DOUBLE) {
			return ctx.mkRealConst(varName);
		} else if (varType == VarType.BOOLEAN) {
			return ctx.mkBoolConst(varName);
		} else if (varType == VarType.ARRAY) {
			return ctx
					.mkArrayConst(varName, ctx.getIntSort(), ctx.getIntSort());
		} else if (varType == VarType.HASH) {
			ArraySort as = ctx.mkArraySort(ctx.getIntSort(), ctx.getIntSort());
			return ctx.mkArrayConst(varName, ctx.getIntSort(), as);
		}
		return null;
	}

	public static Object getValue(Context ctx, Model model, VarType varType,
			Expr var) throws Z3Exception {
		if (varType == VarType.INTEGER) {
			int value = ((IntNum) model.getConstInterp(var)).getInt();
			return value;
		} else if (varType == VarType.DOUBLE) {
			RatNum rn = ((RatNum) model.getConstInterp(var));
			int numerator = rn.getNumerator().getInt();
			int denominator = rn.getDenominator().getInt();
			if (denominator == 0) {
				return 0.0;
			}
			Double value = ((double) numerator) / denominator;
			return value;
		} else if (varType == VarType.BOOLEAN) {
			// TODO
			return null;
		} else if (varType == VarType.ARRAY) {
			return StringUtil.explainArray(ctx, model, (ArrayExpr) var);
		} else if (varType == VarType.HASH) {
			return StringUtil.explainHash(ctx, model, (ArrayExpr) var);
		} else {
			return null;
		}
	}

	public static String printObjects(Object[] inputs) {
		if (inputs == null || inputs.length == 0) {
			return "";
		}
		StringBuffer sb = new StringBuffer();
		for (Object in : inputs) {
			if (in.getClass().isArray()) {
				sb.append(printString((int[]) in));
			} else {
				sb.append(in);
			}
			sb.append(",");
		}
		sb.delete(sb.length() - 1, sb.length());
		return sb.toString();
	}

	public static String printString(int[] string) {
		if (string == null || string.length == 0) {
			return "";
		}

		StringBuffer sb = new StringBuffer();
		sb.append("\"");
		for (int i = 0; i < string.length; i++) {
			sb.append(string[i]);
		}
		sb.append("\"");

		return sb.toString();
	}

	public static String printMap(Map<Integer, int[]> map) {
		StringBuffer sb = new StringBuffer();
		for (int key : map.keySet()) {
			sb.append(key + "->");
			sb.append(printString(map.get(key)));
			sb.append(", ");
		}
		return sb.toString();
	}

	@SuppressWarnings("unchecked")
	public static String printObject(Object o) {
		if (o.getClass().isArray()) {
			return printString((int[]) o);
		} else if (o instanceof Map) {
			return printMap((Map<Integer, int[]>) o);
		} else {
			return o.toString();
		}
	}
}
