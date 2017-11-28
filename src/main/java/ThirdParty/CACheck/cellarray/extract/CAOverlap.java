package ThirdParty.CACheck.cellarray.extract;

import java.util.ArrayList;
import java.util.List;

import ThirdParty.CACheck.CellArray;

public class CAOverlap {

	public static void analyzeOverlap(List<CAResult> allCARs) {
		// analyze overlap cell arrays.
		for (int i = 0; i < allCARs.size(); i++) {
			CellArray ca1 = allCARs.get(i).cellArray;
			for (int j = i + 1; j < allCARs.size(); j++) {
				CellArray ca2 = allCARs.get(j).cellArray;
				if (overlap(ca1, ca2)) {
					allCARs.get(i).isOverlap = true;
					allCARs.get(j).isOverlap = true;
				}
			}
		}

		for (int i = 0; i < allCARs.size(); i++) {
			CAResult car = allCARs.get(i);
			if (car.isOverlap) {
				List<CAResult> overlapCAs = getOverlapCA(car, allCARs);
				if (car.cellArray.size() == overlapCAs.size()) {
					car.isAllOverlap = true;
				}
			}
		}
	}

	public static List<CAResult> removeEqualCellArray(List<CAResult> allCARs) {
		analyzeOverlap(allCARs);

		for (int i = 0; i < allCARs.size(); i++) {
			CAResult car = allCARs.get(i);
			if (car.isAllOverlap) {
				boolean deleted = false;
				List<CAResult> overlapCAs = getOverlapCA(car, allCARs);
				CAResult overlapCA = overlapCAs.get(0);
				if (!car.isSameRowOrColumn) {
					if (overlapCA.isSameRowOrColumn) {
						deleted = true;
					} else if (car.oppositeInputs > overlapCA.oppositeInputs) {
						deleted = true;
					} else if (car.oppositeInputs == overlapCA.oppositeInputs
							&& car.cellArray.size() <= overlapCA.cellArray
									.size()) {
						deleted = true;
					}
				}

				if (deleted) {
					for (CAResult tmp : overlapCAs) {
						tmp.isAllOverlap = false;
					}
					allCARs.remove(i);
					i--;
				}
			}
		}

		// clear all the overlap marks.
		for (int i = 0; i < allCARs.size(); i++) {
			CAResult car = allCARs.get(i);
			car.isAllOverlap = false;
			car.isOverlap = false;
		}

		return allCARs;
	}

	public static List<CAResult> removeEqualCellArray2(List<CAResult> allCARs) {
		analyzeOverlap(allCARs);

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
					if (overlap(curCar.cellArray, tmp.cellArray)) {
						groupCARs.add(tmp);
						all.remove(tmp);
						j--;
					}
				}
			}

			ret.addAll(groupProcessFilter(groupCARs));
		}

		// clear all the overlap marks.
		for (int i = 0; i < ret.size(); i++) {
			CAResult car = ret.get(i);
			car.isAllOverlap = false;
			car.isOverlap = false;
		}
		return ret;
	}

	private static int minOls = Integer.MAX_VALUE;
	private static List<List<CAResult>> carResults = null;

	public static List<CAResult> groupProcessFilter(List<CAResult> groupCARs) {

		minOls = Integer.MAX_VALUE;
		carResults = null;

		List<CAResult> curCARs = new ArrayList<CAResult>();
		List<CAResult> restCARs = new ArrayList<CAResult>();
		for (CAResult car : groupCARs) {
			if (!car.isAllOverlap) {
				curCARs.add(car);
			} else {
				restCARs.add(car);
			}
		}

		internalGroupFilter(curCARs, restCARs, groupCARs);

		if (carResults.size() == 1) {
			return carResults.get(0);
		}
		for (List<CAResult> cars : carResults) {
			boolean allSame = true;
			for (CAResult car : cars) {
				if (!car.isSameRowOrColumn) {
					allSame = false;
					break;
				}
			}
			if (allSame) {
				return cars;
			}
		}

		int ops = Integer.MAX_VALUE;
		List<CAResult> ret = null;
		for (List<CAResult> cars : carResults) {
			int tmp = 0;
			for (CAResult car : cars) {
				tmp += car.oppositeInputs;
			}
			if (tmp < ops) {
				ops = tmp;
				ret = cars;
			} else if (tmp == ops) {
				if (cars.size() < ret.size()) {
					ret = cars;
				}
			}
		}

		return ret;
	}

	public static void internalGroupFilter(List<CAResult> curCARs,
			List<CAResult> restCARs, List<CAResult> groupCARs) {

		int nowOls = computeOverlap(curCARs);
		if (nowOls > minOls) {
			return;
		}

		List<CAResult> curResult = new ArrayList<CAResult>();
		curResult.addAll(curCARs);

		List<CAResult> rest = new ArrayList<CAResult>();
		rest.addAll(restCARs);

		// for covered cell arrays
		for (int i = 0; i < rest.size();) {
			CAResult car = rest.get(i);
			List<CAResult> overlaps = getOverlapCA(car, curCARs);
			if (overlaps.size() == car.cellArray.size()) { // all covered
				rest.remove(i);
			} else {
				i++;
			}
		}

		if (rest.size() == 0) {
			boolean covered = allCovered(curResult, groupCARs);
			if (covered) {
				int ols = computeOverlap(curResult);
				if (ols < minOls) {
					minOls = ols;
					carResults = new ArrayList<List<CAResult>>();
					carResults.add(curResult);
				} else if (ols == minOls) {
					carResults.add(curResult);
				}
			}
		}

		if (rest.size() != 0) {
			CAResult car = rest.remove(0);
			{
				// don't add this ca.
				// all cas overlaped with this ca must be selected
				List<CAResult> tmpRest = new ArrayList<CAResult>();
				tmpRest.addAll(rest);
				List<CAResult> tmpCur = new ArrayList<CAResult>();
				tmpCur.addAll(curResult);
				List<CAResult> ovs = getOverlapCA(car, tmpRest);
				tmpCur.addAll(ovs);
				tmpRest.removeAll(ovs);
				internalGroupFilter(tmpCur, tmpRest, groupCARs);
			}

			{
				// add this ca.
				curResult.add(car);
				internalGroupFilter(curResult, rest, groupCARs);
			}
		}
	}

	public static boolean allCovered(List<CAResult> curCARs,
			List<CAResult> groupCARs) {
		List<CAResult> allCARs = new ArrayList<CAResult>();
		allCARs.addAll(groupCARs);

		for (CAResult car : groupCARs) {
			if (curCARs.contains(car)) {
				allCARs.remove(car);
			}
		}

		for (CAResult car : allCARs) {
			List<CAResult> overlaps = getOverlapCA(car, curCARs);
			if (overlaps.size() != car.cellArray.size()) { // not all covered
				return false;
			}
		}

		return true;
	}

	public static int computeOverlap(List<CAResult> curCARs) {

		int num = 0;
		for (int i = 0; i < curCARs.size() - 1; i++) {
			CAResult car1 = curCARs.get(i);
			for (int j = i + 1; j < curCARs.size(); j++) {
				CAResult car2 = curCARs.get(j);
				if (overlap(car1.cellArray, car2.cellArray)) {
					num++;
				}
			}
		}

		return num;
	}

	public static List<CAResult> getOverlapCA(CAResult car,
			List<CAResult> allCARs) {
		List<CAResult> overlaps = new ArrayList<CAResult>();
		for (CAResult tmp : allCARs) {
			if (tmp != car) {
				if (overlap(car.cellArray, tmp.cellArray)) {
					overlaps.add(tmp);
				}
			}
		}
		return overlaps;
	}

	public static boolean overlap(CellArray ca1, CellArray ca2) {
		if (ca1.extendStart == -1) {
			ca1.extendStart = ca1.start;
		}
		if (ca2.extendStart == -1) {
			ca2.extendStart = ca2.start;
		}

		if (ca1.isRowCA == true && ca2.isRowCA == true || ca1.isRowCA == false
				&& ca2.isRowCA == false) {
			// different row or column
			if (ca1.rowOrColumn != ca2.rowOrColumn) {
				return false;
			}
			// same row or column
			if (ca1.extendStart > ca2.end || ca1.end < ca2.extendStart) {
				return false;
			} else {
				return true;
			}
		}

		if (ca1.isRowCA == true && ca2.isRowCA == false) {
			if (ca1.rowOrColumn < ca2.extendStart || ca1.rowOrColumn > ca2.end) {
				return false;
			}
			if (ca1.extendStart > ca2.rowOrColumn || ca1.end < ca2.rowOrColumn) {
				return false;
			}
			return true;
		}

		if (ca1.isRowCA == false && ca2.isRowCA == true) {
			if (ca2.rowOrColumn < ca1.extendStart || ca2.rowOrColumn > ca1.end) {
				return false;
			}
			if (ca2.extendStart > ca1.rowOrColumn || ca2.end < ca1.rowOrColumn) {
				return false;
			}
			return true;
		}
		return true;
	}
}
