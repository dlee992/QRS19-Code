package ThirdParty.CACheck.cellarray.extract;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import org.apache.poi.ss.usermodel.Cell;

import ThirdParty.CACheck.CellArray;
import ThirdParty.CACheck.amcheck.AnalysisPattern;
import ThirdParty.CACheck.util.Log;
import ThirdParty.synthesis.util.Z3Util;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.IntExpr;
import com.microsoft.z3.Model;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Status;
import com.microsoft.z3.Z3Exception;

public class PostFilter {

	public static List<CAResult> postProcessFilter(List<CAResult> allCARs,
			List<CellArray> correctCAs, AnalysisPattern analysisPattern) {

		List<CAResult> remaining = new ArrayList<CAResult>();
		remaining.addAll(allCARs);

		if (analysisPattern.curHeuristic == AnalysisPattern.NO) {
			// Don't filter.
			// Do nothing.
		} else if (analysisPattern.curHeuristic == AnalysisPattern.OVERLAP) {
			// Filter out overlap equal CAs.
			// remaining = CAOverlap.removeEqualCellArray(remaining);
			remaining = CAOverlap.removeEqualCellArray2(remaining);
		} else {

			for (int i = 0; i < remaining.size(); i++) {
				CAResult car = remaining.get(i);
				if (car.isAmbiguous) {
					if (car.isOpposite()
							|| AmbiguousDetector.oppositeInputs(car,
									car.pattern, false) >= 1
							&& car.percentage < 0.7) {
						// TODO for (a-b)/b doesn't work now.
						remaining.remove(i);
						i--;
					}
				}
			}

			remaining = multiThreadPostProcessFilter(remaining, correctCAs, analysisPattern);

			// filter out some not good cell arrays.
			if (analysisPattern.aggressiveFilter) {
				for (int i = 0; i < remaining.size(); i++) {
					CAResult car = remaining.get(i);
					boolean deleted = false;
					if (car.correctCells.size() == 1) {
						if (car.errorCells.size() == car.cellArray.size() - 1) {
							// deleted = true;
						} else if (!car.isSameRowOrColumn && car.isMissing) {
							boolean allZero = true;
							for (Cell cell : car.plainValueCells) {
								if (cell.getNumericCellValue() != 0.0) {
									allZero = false;
									break;
								}
							}
							if (allZero) {
								deleted = true;
							}
						}
					}

					if (deleted) {
						remaining.remove(i);
						i--;
					}
				}
			}
		}

		if (analysisPattern.resultType == AnalysisPattern.Result_All) {
			// all cell arrays
			// do nothing
		} else if (analysisPattern.resultType == AnalysisPattern.Result_Same) {
			// the same cell arrays
			for (int i = 0; i < remaining.size(); i++) {
				CAResult car = remaining.get(i);
				if (!car.isSameRowOrColumn) {
					remaining.remove(i);
					i--;
				}
			}
		} else if (analysisPattern.resultType == AnalysisPattern.Result_Diff) {
			// the diff cell arrays
			for (int i = 0; i < remaining.size(); i++) {
				CAResult car = remaining.get(i);
				if (car.isSameRowOrColumn) {
					remaining.remove(i);
					i--;
				}
			}
		} else if (analysisPattern.resultType == AnalysisPattern.Result_Opposite) {
			// the opposite cell arrays
			for (int i = 0; i < remaining.size(); i++) {
				CAResult car = remaining.get(i);
				if (!car.isOpposite()) {
					remaining.remove(i);
					i--;
				}
			}
		}

		return remaining;
	}

	public static List<CAResult> multiThreadPostProcessFilter(
			final List<CAResult> allCARs, final List<CellArray> correctCAs, final AnalysisPattern analysisPattern) {
		ExecutorService executor = Executors.newFixedThreadPool(2);
		FutureTask<List<CAResult>> task1 = new FutureTask<List<CAResult>>(
				new Callable<List<CAResult>>() {
					public List<CAResult> call() throws Exception {
						return postProcessFilter1(allCARs, correctCAs, analysisPattern);
					}
				});

		/*
		 * cases: 287, 40693, 5689
		 */
		FutureTask<List<CAResult>> task2 = new FutureTask<List<CAResult>>(
				new Callable<List<CAResult>>() {
					public List<CAResult> call() throws Exception {
						Thread.sleep(120000);
						return postProcessFilter2(allCARs, correctCAs, false, analysisPattern);
					}
				});
		executor.execute(task1);
		executor.execute(task2);
		executor.shutdown();

		boolean isDone = false;
		List<CAResult> ret = null;
		while (!isDone) {
			isDone = task1.isDone();
			if (isDone) {
				try {
					ret = task1.get();
					// task2.cancel(true);
					break;
				} catch (Exception e) {
				}
			}

			isDone = task2.isDone();
			if (isDone) {
				try {
					ret = task2.get();
					// task1.cancel(true);
					break;
				} catch (Exception e) {
				}
			}
			try {
				Thread.sleep(1000);
			} catch (Exception e) {
			}
		}

		return ret;
	}

	public static List<CAResult> postProcessFilter1(List<CAResult> allCARs,
			List<CellArray> correctCAs, AnalysisPattern analysisPattern) {
		List<CAResult> all = new ArrayList<CAResult>();
		all.addAll(allCARs);

		List<CAResult> ret = new ArrayList<CAResult>();
		while (all.size() != 0) {
			List<CAResult> groupCARs = new ArrayList<CAResult>();
			groupCARs.add(all.remove(0));
			for (int i = 0; i < groupCARs.size(); i++) {
				CAResult curCar = groupCARs.get(i);
				for (int j = 0; j < all.size(); j++) {
					CAResult tmp = all.get(j);
					if (CAOverlap.overlap(curCar.cellArray, tmp.cellArray)) {
						groupCARs.add(tmp);
						all.remove(tmp);
						j--;
					}
				}
			}

			ret.addAll(groupProcessFilter(groupCARs, correctCAs, analysisPattern));
		}

		return ret;
	}

	private static int minCE = Integer.MAX_VALUE;
	private static List<List<CAResult>> carResults = null;

	public static List<CAResult> groupProcessFilter(List<CAResult> groupCARs,
			List<CellArray> correctCAs, AnalysisPattern analysisPattern) {

		minCE = Integer.MAX_VALUE;
		carResults = null;

		internalGroupFilter(new ArrayList<CAResult>(), groupCARs, correctCAs,
				groupCARs, analysisPattern);

		/*
		for (List<CAResult> cars : carResults) {
			boolean allCorrect = true;
			for (CAResult car : cars) {
				if (car.isAmbiguous) {
					allCorrect = false;
					break;
				}
			}
			if (allCorrect) {
				return cars;
			}
		}
		*/

		/*
		 * for (List<CAResult> cars : carResults) { boolean allSame = true; for
		 * (CAResult car : cars) { if (!car.isSameRowOrColumn) { allSame =
		 * false; break; } } if (allSame) { return cars; } }
		 */

		int ce = Integer.MAX_VALUE;
		List<List<CAResult>> rets = null;
		for (List<CAResult> cars : carResults) {
			int tmp = computeCE(cars, correctCAs, false, false, analysisPattern);
			if (tmp < ce) {
				ce = tmp;
				rets = new ArrayList<List<CAResult>>();
				rets.add(cars);
			} else if (tmp == ce) {
				rets.add(cars);
			}
		}

		List<CAResult> ret = null;
		double length = Integer.MIN_VALUE;
		for (List<CAResult> cars : rets) {
			int total_length = 0;
			for (CAResult car : cars) {
				total_length += car.cellArray.size();
			}
			double ave_length = ((double) total_length) / cars.size();
			if (ave_length > length) {
				length = ave_length;
				ret = cars;
			}
		}

		return ret;
	}

	public static void internalGroupFilter(List<CAResult> curCARs,
			List<CAResult> restCARs, List<CellArray> correctCAs,
			List<CAResult> groupCARs, AnalysisPattern analysisPattern) {

		int nowCe = computeCE(curCARs, correctCAs, false, true, analysisPattern);
		if (nowCe > minCE) {
			return;
		}

		List<CAResult> curResult = new ArrayList<CAResult>();
		curResult.addAll(curCARs);

		List<CAResult> rest = new ArrayList<CAResult>();
		rest.addAll(restCARs);

		// for all non-conflicted cell arrays
		for (int i = 0; i < rest.size();) {
			CAResult car = rest.get(i);
			List<CAResult> overlaps = CAOverlap.getOverlapCA(car, rest);
			if (overlaps.size() == 0) {
				rest.remove(i);
				curResult.add(car);
			} else {
				i++;
			}
		}

		if (rest.size() == 0) {
			// some cas are deleted too early.
			for (CAResult car : groupCARs) {
				if (!curResult.contains(car)
						&& CAOverlap.getOverlapCA(car, curResult).size() == 0) {
					curResult.add(car);
				}
			}
			int ce = computeCE(curResult, correctCAs, false, false, analysisPattern);
			if (ce < minCE) {
				minCE = ce;
				carResults = new ArrayList<List<CAResult>>();
				carResults.add(curResult);
			} else if (ce == minCE) {
				carResults.add(curResult);
			}
		}

		// for all conflicted cell arrays
		if (rest.size() != 0) {
			CAResult car = rest.remove(0);

			{
				// don't add this ca.
				// at least one of ovs must be selected, so, if a cell array can
				// overlap with all cas in ovs, then it should be deleted
				List<CAResult> tmpRest = new ArrayList<CAResult>();
				tmpRest.addAll(rest);
				List<CAResult> ovs = CAOverlap.getOverlapCA(car, tmpRest);
				for (int i = 0; i < tmpRest.size(); i++) {
					CAResult now = tmpRest.get(i);
					if (ovs.contains(now)) {
						continue;
					}
					boolean allOv = true;
					for (CAResult tmp : ovs) {
						if (!CAOverlap.overlap(now.cellArray, tmp.cellArray)) {
							allOv = false;
							break;
						}
					}
					if (allOv) {
						tmpRest.remove(now);
						i--;
					}
				}
				internalGroupFilter(curResult, tmpRest, correctCAs, groupCARs, analysisPattern);
			}

			{
				// add this ca.
				List<CAResult> tmpRest = new ArrayList<CAResult>();
				tmpRest.addAll(rest);

				curResult.add(car);
				tmpRest.removeAll(CAOverlap.getOverlapCA(car, tmpRest));
				internalGroupFilter(curResult, tmpRest, correctCAs, groupCARs, analysisPattern);
			}
		}
	}

	// (1) one cell only belong to one cell array.
	// (2) most cell's values should be correct.
	// (3) Find most cell arrays
	public static int computeCE(List<CAResult> curResult,
			List<CellArray> correctCAs, boolean directed, boolean onlyError, AnalysisPattern analysisPattern) {

		int sum = 0;

		int curHeuristic = analysisPattern.curHeuristic;
		for (int i = 0; i < curResult.size(); i++) {
			CAResult car = curResult.get(i);
			if (curHeuristic == AnalysisPattern.Conformance_Cell
					|| curHeuristic == AnalysisPattern.Conformance_And_Correct_Cell) {
				/**
				 * 1. Minimize the conformance errors.
				 */
				sum += car.errorCells.size();
			} else if (curHeuristic == AnalysisPattern.Ambiguous_Cell) {
				/**
				 * 2. Minimize the ambiguous errors.
				 */
				sum += car.ambiguousCells.size();
			} else if (curHeuristic == AnalysisPattern.Ambiguous_Cell_ReferChange) {
				/**
				 * 3. Refer changed is not wrong. Refer changed potential wrong,
				 * but it is right now.
				 */
				sum += car.ambiguousCells.size() /*- car.referChangedCells.size()*/;
			}

			/**
			 * 5. We don't like the opposite cell arrays, if yes, we degrade it.
			 */
			if (car.oppositeInputs > 0) {
				// sum += car.oppositeInputs;
			}
		}

		/**
		 * 4. For the correct cell arrays, they should be in the final results.
		 * Otherwise, we should degrade the results.
		 */
		if (curHeuristic == AnalysisPattern.Conformance_And_Correct_Cell
				&& !onlyError) {
			Set<Key> checked = new HashSet<Key>();

			for (CellArray ca : correctCAs) {
				for (int index = ca.start; index <= ca.end; index++) {
					int row = 0;
					int column = 0;
					if (ca.isRowCA) {
						row = ca.rowOrColumn;
						column = index;
					} else {
						row = index;
						column = ca.rowOrColumn;
					}

					Key key = new Key(row, column);
					if (checked.contains(key)) {
						continue;
					} else {
						checked.add(key);
					}

					List<CAResult> coveredCARs = getCoveredCA(curResult, row,
							column);
					if (coveredCARs.size() == 0) {
						sum += 1;
					} else {
						if (directed) {
							boolean covered = false;
							List<CellArray> corCoveredCAs = getCorrectCoveredCA(
									correctCAs, row, column);
							boolean both = corCoveredCAs.size() == 2;
							for (CAResult tmp : coveredCARs) {
								if (both || !both
										&& tmp.cellArray.isRowCA == ca.isRowCA) {
									covered = true;
								}
							}
							if (!covered) {
								sum += 1;
							}
						}
					}

				}
			}
		}
		
		for (CAResult car : curResult) {
			for (CAResult compCar : car.compensateCAs) {
				if (!curResult.contains(compCar)) {
					sum++;
				}
			}
		}
		
		return sum;
	}

	// (1) one cell only belong to one cell array.
	// (2) most cell's values should be correct.
	// (3) Find most cell arrays
	public static List<CAResult> postProcessFilter2(List<CAResult> allCARs,
			List<CellArray> correctCAs, boolean directed, AnalysisPattern analysisPattern) {

		try {
			Context ctx = Z3Util.getContext();

			List<BoolExpr> z3Vars = new ArrayList<BoolExpr>();
			for (int i = 0; i < allCARs.size(); i++) {
				BoolExpr isWell = ctx.mkBoolConst("ca_" + i);
				z3Vars.add(isWell);
			}

			Solver solver = ctx.mkSolver();

			// If a cell array is correct, we should include it.
			// Wrong assumption.
			// But the performance is bad, so relax it to same row/column

			for (int i = 0; i < allCARs.size(); i++) {
				CAResult car = allCARs.get(i);
				if (!car.isAmbiguous && car.isSameRowOrColumn) {
					solver.add(z3Vars.get(i));
				}
			}

			for (int i = 0; i < allCARs.size(); i++) {
				CAResult car = allCARs.get(i);
				List<CAResult> overlaps = CAOverlap.getOverlapCA(car, allCARs);
				// the overlaps can't coexist.
				for (CAResult tmp : overlaps) {
					// correct cell arrays can conflict.
					// TODO wrong. Can't overlap too.
					// if (!car.isAmbiguous && !tmp.isAmbiguous) {
					// continue;
					// }
					int index = allCARs.indexOf(tmp);
					BoolExpr coexist = ctx.mkAnd(z3Vars.get(i),
							z3Vars.get(index));
					BoolExpr noCoexit = ctx.mkNot(coexist);
					solver.add(noCoexit);
				}

				// for the overlap group, at least one is selected
				BoolExpr atLeastOne = z3Vars.get(i);
				for (CAResult tmp : overlaps) {
					int index = allCARs.indexOf(tmp);
					atLeastOne = ctx.mkOr(atLeastOne, z3Vars.get(index));
				}
				solver.add(atLeastOne);
			}

			IntExpr sum = ctx.mkIntConst("sum");
			solver.add(ctx.mkEq(sum, ctx.mkInt(0)));

			int curHeuristic = analysisPattern.curHeuristic;
			for (int i = 0; i < allCARs.size(); i++) {
				CAResult car = allCARs.get(i);
				if (curHeuristic == AnalysisPattern.Conformance_Cell
						|| curHeuristic == AnalysisPattern.Conformance_And_Correct_Cell) {
					/**
					 * 1. Minimize the conformance errors.
					 */
					sum = (IntExpr) ctx.mkITE(z3Vars.get(i),
							ctx.mkAdd(sum, ctx.mkInt(car.errorCells.size())),
							sum);
				} else if (curHeuristic == AnalysisPattern.Ambiguous_Cell) {
					/**
					 * 2. Minimize the ambiguous errors.
					 */
					sum = (IntExpr) ctx.mkITE(z3Vars.get(i), ctx.mkAdd(sum,
							ctx.mkInt(car.ambiguousCells.size())), sum);
				} else if (curHeuristic == AnalysisPattern.Ambiguous_Cell_ReferChange) {
					/**
					 * 3. Refer changed is not wrong. Refer changed potential
					 * wrong, but it is right now.
					 */
					sum = (IntExpr) ctx.mkITE(z3Vars.get(i),
							ctx.mkAdd(sum, ctx.mkInt(car.ambiguousCells.size()
							/*- car.referChangedCells.size()*/)), sum);
				}

				/**
				 * 5. We don't like the opposite cell arrays, if yes, we degrade
				 * it.
				 */
				if (car.oppositeInputs > 0) {
					// sum = (IntExpr) ctx.mkITE(z3Vars.get(i),
					// ctx.mkAdd(sum, ctx.mkInt(car.oppositeInputs)), sum);
				}
			}

			/**
			 * 4. For the correct cell arrays, they should be in the final
			 * results. Otherwise, we should degrade the results.
			 */
			if (curHeuristic == AnalysisPattern.Conformance_And_Correct_Cell) {
				Set<Key> checked = new HashSet<Key>();
				for (CellArray ca : correctCAs) {
					for (int index = ca.start; index <= ca.end; index++) {
						int row = 0;
						int column = 0;
						if (ca.isRowCA) {
							row = ca.rowOrColumn;
							column = index;
						} else {
							row = index;
							column = ca.rowOrColumn;
						}

						Key key = new Key(row, column);
						if (checked.contains(key)) {
							continue;
						} else {
							checked.add(key);
						}

						List<CAResult> coveredCARs = getCoveredCA(allCARs, row,
								column);
						if (coveredCARs.size() == 0) {
							sum = (IntExpr) ctx.mkAdd(sum, ctx.mkInt(1));
						}

						// We don't consider the direction now. Just don't lose
						// the semantic.
						else if (coveredCARs.size() == 1) {
							CAResult tmp = coveredCARs.get(0);
							int tmpIndex = allCARs.indexOf(tmp);
							if (tmp.cellArray.isRowCA == ca.isRowCA) {
								sum = (IntExpr) ctx.mkITE(z3Vars.get(tmpIndex),
										sum, ctx.mkAdd(sum, ctx.mkInt(1)));
							} else {
								sum = (IntExpr) ctx.mkAdd(sum, ctx.mkInt(1));
							}
						} else {
							BoolExpr covered = ctx.mkBool(false);
							for (CAResult tmp : coveredCARs) {
								if (directed) {
									List<CellArray> corCoveredCAs = getCorrectCoveredCA(
											correctCAs, row, column);
									boolean both = corCoveredCAs.size() == 2;
									if (both
											|| !both
											&& tmp.cellArray.isRowCA == ca.isRowCA) {
										int tmpIndex = allCARs.indexOf(tmp);
										covered = ctx.mkOr(covered,
												z3Vars.get(tmpIndex));
									}

								} else {
									int tmpIndex = allCARs.indexOf(tmp);
									covered = ctx.mkOr(covered,
											z3Vars.get(tmpIndex));
								}
							}
							sum = (IntExpr) ctx.mkITE(covered, sum,
									ctx.mkAdd(sum, ctx.mkInt(1)));
						}
					}
				}
			}

			int errNum = 0;
			int quick = 30;
			for (;; errNum += quick) {
				solver.push();
				solver.add(ctx.mkLe(sum, ctx.mkInt(errNum)));
				Status status = Z3Util.execute(ctx, solver, 1, false);
				solver.pop();
				if (status == Status.SATISFIABLE) {
					break;
				}
			}

			errNum -= quick;
			if (errNum < 0) {
				errNum = 0;
			}
			for (;; errNum += 1) {
				solver.push();
				solver.add(ctx.mkLe(sum, ctx.mkInt(errNum)));
				Status status = Z3Util.execute(ctx, solver, 1, false);
				if (status == Status.SATISFIABLE) {
					Model model = solver.getModel();
					List<CAResult> finalCARs = new ArrayList<CAResult>();
					for (int i = 0; i < allCARs.size(); i++) {
						BoolExpr expr = (BoolExpr) model.getConstInterp(z3Vars
								.get(i));
						if (expr.toString().equals("true")) {
							finalCARs.add(allCARs.get(i));
						}
					}
					return finalCARs;
				}
				solver.pop();
			}
		} catch (Z3Exception e) {
			Log.logNewLine(e, Log.writer);
		}
		return new ArrayList<CAResult>();
	}

	private static List<CAResult> getCoveredCA(List<CAResult> allCARs, int row,
			int column) {
		List<CAResult> res = new ArrayList<CAResult>();
		for (CAResult car : allCARs) {
			CellArray ca = car.cellArray;
			if (ca.isRowCA) {
				if (ca.rowOrColumn == row && ca.start <= column
						&& ca.end >= column) {
					res.add(car);
				}
			} else {
				if (ca.rowOrColumn == column && ca.start <= row
						&& ca.end >= row) {
					res.add(car);
				}
			}
		}
		return res;
	}

	public static List<CellArray> getCorrectCoveredCA(List<CellArray> allCAs,
			int row, int column) {
		List<CellArray> res = new ArrayList<CellArray>();
		for (CellArray ca : allCAs) {
			if (ca.isRowCA) {
				if (ca.rowOrColumn == row && ca.start <= column
						&& ca.end >= column) {
					res.add(ca);
				}
			} else {
				if (ca.rowOrColumn == column && ca.start <= row
						&& ca.end >= row) {
					res.add(ca);
				}
			}
		}
		return res;
	}

	static class Key {
		int row;
		int column;

		Key(int row, int column) {
			this.row = row;
			this.column = column;
		}

		@Override
		public int hashCode() {
			return (row + "------" + column).hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			Key k = (Key) obj;
			if (k.row == row && k.column == column) {
				return true;
			}
			return false;
		}
	}
}
