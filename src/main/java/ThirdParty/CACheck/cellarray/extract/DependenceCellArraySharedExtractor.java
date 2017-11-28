package ThirdParty.CACheck.cellarray.extract;

import java.util.ArrayList;
import java.util.List;

import ThirdParty.CACheck.AMSheet;
import ThirdParty.CACheck.CellArray;
import ThirdParty.CACheck.R1C1Cell;
import ThirdParty.CACheck.amcheck.AnalysisPattern;

public class DependenceCellArraySharedExtractor extends
		DependenceCellArrayAbstractExtractor {

	public DependenceCellArraySharedExtractor(AMSheet sheet, AnalysisPattern analysisPattern) {
		super(sheet, analysisPattern);
	}

	public boolean isCellArray(CellArray ca, List<Dependence> deps) {

		if (deps.size() <= 1) {
			return true;
		}

		// For each pair of cells, if they have shared dependence, they may
		// belong to the cell array.
		// If cell1 and cell2 don't have any shared dependence, there should
		// exist cell3, which has shared dependence with cell1, and cell2, too.

		// share constants
		List<R1C1Cell> constants = DependenceConstantExtractor
				.extractConstants(deps);
		// share possible constants
		List<R1C1Cell> posConstants = DependenceConstantExtractor
				.extractPossibleConstants(deps, 0.75f);

		for (Dependence tmpDep : deps) {
			for (R1C1Cell cell : tmpDep.dependedCells) {
				boolean posCons = false;
				for (R1C1Cell con : posConstants) {
					if (con.getRelativeA1Cell()
							.equals(cell.getRelativeA1Cell())) {
						posCons = true;
						break;
					}
				}
				if (constants.contains(cell) || posCons) {
					continue;
				}
				if (ca.isRowCA) {
					cell.columnRelative = true;
				} else {
					cell.rowRelative = true;
				}
			}
		}

		// Simple case: all the cells should be similar with the first cell.
		Dependence firstDep = deps.get(0);
		for (int j = 1; j < deps.size(); j++) {
			boolean similar = false;
			Dependence dep = deps.get(j);
			if (similarDependence(ca.isRowCA, deps, firstDep, dep, constants,
					posConstants)) {
				similar = true;
			} else {
				for (Dependence otherDep : deps) {
					if (otherDep == firstDep || otherDep == dep) {
						continue;
					}
					if (similarDependence(ca.isRowCA, deps, firstDep, otherDep,
							constants, posConstants)
							&& similarDependence(ca.isRowCA, deps, dep,
									otherDep, constants, posConstants)) {
						similar = true;
						break;
					}
				}
			}
			if (!similar) {
				return false;
			}
		}

		return true;
	}

	private boolean similarDependence(boolean rowCA, List<Dependence> deps,
			Dependence dep1, Dependence dep2, List<R1C1Cell> constants,
			List<R1C1Cell> posConstants) {

		List<R1C1Cell> depCell1 = new ArrayList<R1C1Cell>();
		depCell1.addAll(dep1.dependedCells);
		List<R1C1Cell> depCell2 = new ArrayList<R1C1Cell>();
		depCell2.addAll(dep2.dependedCells);

		for (R1C1Cell con : constants) {
			if (depCell1.contains(con) && depCell2.contains(con)) {
				return true;
			}
		}

		for (R1C1Cell con : posConstants) {
			int i1 = 0;
			int i2 = 0;
			for (; i1 < depCell1.size(); i1++) {
				if (con.getRelativeA1Cell().equals(
						depCell1.get(i1).getRelativeA1Cell())) {
					break;
				}
			}
			for (; i2 < depCell2.size(); i2++) {
				if (con.getRelativeA1Cell().equals(
						depCell2.get(i2).getRelativeA1Cell())) {
					break;
				}
			}

			if (i1 < depCell1.size() && i2 < depCell2.size()) {
				return true;
			}
		}

		// share r1c1 cell.
		for (int i = 0; i < depCell1.size(); i++) {
			R1C1Cell c1 = depCell1.get(i);
			if (depCell2.contains(c1)) {
				return true;
			}
		}

		return false;
	}
}
