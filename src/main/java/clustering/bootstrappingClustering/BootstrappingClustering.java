package clustering.bootstrappingClustering;

import entity.FakeCell;
import entity.CellFeature;
import entity.CellLocation;
import entity.Cluster;
import featureExtraction.FeatureExtraction;
import featureExtraction.weakFeatureExtraction.HeaderExtraction;
import featureExtraction.weakFeatureExtraction.Snippet;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.descriptive.rank.Max;
import org.apache.commons.math3.stat.descriptive.rank.Min;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.formula.FormulaParseException;
import org.apache.poi.ss.formula.FormulaType;
import org.apache.poi.ss.formula.ptg.AreaPtg;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
import programEntry.GP;
import utility.BasicUtility;
import utility.FormulaParsing;

import java.util.ArrayList;
import java.util.List;

import static Benchmarks.TestEUSES.TIMEOUT;
//import static programEntry.GP.O3;

public class BootstrappingClustering {
	private static Logger logger = LogManager.getLogger(BootstrappingClustering.class.getName());

	private List<Cluster> clusterVector;
	private List<CellReference> cellRefsVector;
	private List<CellFeature> cellFeatureList;
	private List<String> featureVector;
	private Sheet sheetOrigin;

	private BasicUtility bu = new BasicUtility();

	private long timeout = (long) (TIMEOUT * 1_000_000_000.0);
	private long beginTime;
	
	public BootstrappingClustering(FeatureExtraction fe, Sheet sheetOrigin, long beginTime) {
		this.cellRefsVector  = fe.getCellRefsVector();
		this.clusterVector   = fe.getClusterVector();
		this.featureVector   = fe.getFeatureVectorForClustering();
		this.cellFeatureList = fe.getCellFeatureList();
		this.sheetOrigin     = sheetOrigin;
		this.beginTime = beginTime;
	}

	public RealMatrix clustering(RealMatrix featureCellM) {
		RealMatrix cellFeatureMatrixForClustering = splitMatrix(
				featureCellM.transpose(), 
				cellRefsVector, 
				null,
				featureVector).
				transpose();
		
		CellClusterMatrix ccc = new CellClusterMatrix(cellRefsVector,
				clusterVector);
		RealMatrix cellClusterM = ccc.matrixCreation(cellFeatureList);
		
		FeatureClusterMatrix fcm = new FeatureClusterMatrix(
				cellFeatureMatrixForClustering, cellClusterM);
		RealMatrix featureClusterMClustering = fcm.matrixCreation();

		//TODO: try Ochiai or point-wise mutual information
		featureClusterMClustering = fcm.computeOcc(featureClusterMClustering);
//        featureClusterMClustering = fcm.computeNPMI(featureClusterMClustering);

		return ccc.computeMatrix(
				cellFeatureMatrixForClustering, featureClusterMClustering);
	}

	private RealMatrix splitMatrix(RealMatrix rm,
			List<CellReference> selectedCellVector,
			List<Cluster> selectedClusterVector,
			List<String> selectedFeatureVector) {

		int[] selectedRow = null;
		int[] selectedCol = null;
		int i = 0;
		int j = 0;
		if (selectedCellVector != null) {
			selectedRow = new int[selectedCellVector.size()];
			for (CellReference cr : selectedCellVector) {
				selectedRow[i] = cellRefsVector.indexOf(cr);
				i++;
			}
		}
		if (selectedClusterVector != null) {
			selectedCol = new int[selectedClusterVector.size()];
			for (Cluster cluster : selectedClusterVector) {
				selectedCol[j] = clusterVector.indexOf(cluster);
				j++;
			}
		}
		if (selectedFeatureVector != null) {
			selectedCol = new int[selectedFeatureVector.size()];

			for (String ft : selectedFeatureVector) {
				selectedCol[j] = featureVector.indexOf(ft);
				j++;
			}
		}

		return rm.getSubMatrix(selectedRow, selectedCol);
	}

	private int getSnippet(FakeCell fakeCell, List<Snippet> snippetList) {
	    int rowInit = fakeCell.getRow()-2;
	    int columnInit = fakeCell.getColumn();

        for (int i = rowInit; i >= 0; i--) {
            for (int j = 0; j < snippetList.size(); j++) {
                Snippet snippet = snippetList.get(j);
                if (snippet.up <= i && snippet.bottom >= i &&
                        snippet.left <= columnInit && snippet.right >= columnInit) {
                    return j;
                }
            }
        }

        return -1;
    }

    private boolean containAZ(String cellValue) {
        cellValue = cellValue.toLowerCase();
	    for (int i = 0; i < cellValue.length(); i++) {
            if (cellValue.charAt(i) >= 'a' && cellValue.charAt(i) <= 'z')
                return true;
        }

	    return false;
    }

	public List<Cluster> addCellToCluster(RealMatrix cellClusterMF, List<CellReference> nonSeedCellRefs,
			List<Cell> nonSeedCells, double parameter) throws InterruptedException {

		//对整个worksheet进行分割，划分成若干个table单位，然后对其进行编号
		//统计某个cluster中的seed cells位于某几个table中
		//考虑当某个data cell在加入到cluster中时，这个data cell是否来自于一个新的未被cluster覆盖的table
		//如果是新的table，就拒绝它加入到cluster中
//        SnippetExtraction snippetExt = new SnippetExtraction(sheetOrigin);
//        List<Snippet> snippetList = snippetExt.extractSnippet();
//        List<Integer> disjointSet = new ArrayList<>();
//        for (int i = 0; i < snippetList.size(); i++) {
//            disjointSet.add(i);
//        }

        //竟然搞出了一个类似并查集的鬼东西，妈呀
//        for (int i = 0; i < snippetList.size(); i++) {
//            Snippet snippet = snippetList.get(i);
//            boolean flag = false;
//
//            在头两行，去掉最左边一列的剩余cells里，如果有string类型，那就算一个完整的snippet。
//            for (int j = snippet.up; j <= snippet.up + 1; j++) {
//                Row row = sheetOrigin.getRow(j);
//                if (row == null) continue;
//
//                for (int k = snippet.left+1; k <= snippet.right; k++) {
//                    Cell cell = row.getCell(k);
//                    if (cell == null) continue;
//
//                    if (cell.getCellType() == 1 && containAZ(cell.getStringCellValue())) {
//                        flag = true;
//                        break;
//                    }
//                }
//            }
//
//            if (flag) continue;
//
//            不完整
//            int leftRoot = getSnippet(new FakeCell(snippet.left+1, snippet.up), snippetList);
//            int rightRoot = getSnippet(new FakeCell(snippet.right, snippet.up), snippetList);
//            if (leftRoot != rightRoot || leftRoot == -1) continue;
//
//            if (leftRoot <= -1 || leftRoot >= disjointSet.size())
//                System.out.println("IndexError");
//            disjointSet.set(i, leftRoot);
//        }

//        for (int i = 0; i < disjointSet.size(); i++) {
//            Snippet snippet = snippetList.get(i);
//            System.out.printf("index = %d, snippet = %s, disjoint set = %d\n",
//                    i, snippet.toString(), disjointSet.get(i));
//        }

		RealMatrix isolatedCellMatrix = splitMatrix(cellClusterMF,
				nonSeedCellRefs, clusterVector, null);

		List<Cluster> clusters = new ArrayList<Cluster>();

        //TODO: first, harvest formula cells; second, harvest data cells.
        int celltype[] = {2, 0};
        int index = 0;

        while (index < 2) {
			if (Thread.interrupted() || System.nanoTime() - beginTime > timeout) {
				throw new InterruptedException();
			}

            if (isolatedCellMatrix != null) {
                Max max = new Max();
                for (int i1 = 0; i1 < isolatedCellMatrix.getRowDimension(); i1++) {
                    if (celltype[index] != nonSeedCells.get(i1).getCellType()) continue;

                    double[] row = isolatedCellMatrix.getRow(i1);
                    double maxValue = max.evaluate(row);

                    if (maxValue > 0) {
                        for (int j1 = 0; j1 < isolatedCellMatrix.getColumnDimension(); j1++) {
							if (Thread.interrupted() || System.nanoTime() - beginTime > timeout) {
								throw new InterruptedException();
							}

                            if (isolatedCellMatrix.getEntry(i1, j1) == maxValue) {
                                Cluster parentCluster = clusterVector.get(j1);
                                CellReference childCR = nonSeedCellRefs.get(i1);
                                Cell childCell = nonSeedCells.get(i1);

                                Cluster childCluster = new Cluster(
                                        childCR.formatAsString());

                                //FIXME: this following function seems wrong.
                                //FIXME: such as the column A in the sheet "Table II.6" of workbook "01-38-PK...".
                                if (checkWithinClusterReference(parentCluster, childCR, childCell)) {
                                    if (checkConfidence(maxValue, parentCluster, cellClusterMF, parameter)) {
                                        //TODO: if a data cell is referenced by formulas in seed cluster >= 1 times.
                                        //TODO: then filter it out immediately.
                                        //TODO: check if childCell is in the sinppet of parentCluster.
                                        if (atLeastThree(parentCluster, childCell)) {
                                            //out.println("atLeastThree");
                                            continue;
                                        }

//                                        if (index == 1) {
//                                            boolean flag = NumericAndNotOutOfRange(childCell, parentCluster, sheetOrigin);
//                                            if (!flag)
//                                                break;
//                                        }

                                        //找到这个data cell所属的真正snippet，
                                        //找到这个cluster包含的所有snippet，
                                        //是否被这个cluster覆盖的snippet包含。
//                                        if (index == 1) {
//                                            boolean flag = false;
//                                            int childIndex = getSnippetIndexFromCell(childCell, snippetList, disjointSet);
//                                            for (Cell cell:
//                                                 parentCluster.getSeedCells()) {
//                                                int indexSeedCell = getSnippetIndexFromCell(cell, snippetList, disjointSet);
//                                                if (childIndex == indexSeedCell && childIndex != -1) {
//                                                    flag = true;
//                                                    break;
//                                                }
//                                            }
//
//                                            if (!flag) {
//                                                break;
//                                            }
//                                        }

//                                        out.printf("harvest the cell %s with the value %.4f\n",
//												childCR.formatAsString(), maxValue);

//                                        if (index == 1) {
//                                            boolean flag = filterDataCellsByHeaderPosition
//													(childCell, parentCluster, sheetOrigin);
//                                            if (!flag) {
//                                                break;
//                                            }
//                                        }

                                        childCluster.setAssociationValue(maxValue);
                                        parentCluster.addChild(childCluster);

//                                        if (index == 0 && GP.plusFirstSecond) {
//                                            parentCluster.extractCells(childCell.getSheet(), parentCluster, 2);
//                                            parentCluster.extractCellRefs(parentCluster, 2);
//                                        }
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            index ++;
        }

        clusters.addAll(clusterVector);
		return clusters;
	}

	private boolean filterDataCellsByHeaderPosition(Cell cell, Cluster cluster, Sheet sheet) {
	    //得到每个cell的header的起始位置，作为header的标记位置
        //检查cell的header是否被cluster的seed cluster的header set包含
        HeaderExtraction headerExt = new HeaderExtraction(sheet);
	    FakeCell fakeCell = headerExt.findHeaderPosition(cell, 1, 1);

        for (Cell seedCell:
             cluster.getSeedCells()) {
            FakeCell fc = headerExt.findHeaderPosition(seedCell, 1, 1);

            if (fakeCell.getRow() == fc.getRow())
                return true;
        }

	    return false;
    }

	private int getSnippetIndexFromCell(Cell cell, List<Snippet> snippetList, List<Integer> disjointSet) {
        int row = cell.getRowIndex(), column = cell.getColumnIndex();
        int index = -1;
	    for (int i = 0; i < snippetList.size(); i++) {
            Snippet snippet = snippetList.get(i);
            if (snippet.up <= row && snippet.bottom >= row &&
                    snippet.left <= column && snippet.right >= column) {
                index = i;
                break;
            }
        }

        if (index == -1)
            return -1;

        while (disjointSet.get(index) != index) {
	        index = disjointSet.get(index);
        }

	    return index;
    }

//	private boolean doubleAndInteger(Cell cellToAdd, Cluster cluster, Sheet sheet) {
//        int doubleNumber = 0, intNumber = 0;
//	    for (Cell cell:
//             cluster.getSeedCells()) {
//            double ret = cell.getNumericCellValue();
//            if ((ret % 1) == 0) intNumber++;
//            else doubleNumber++;
//        }
//
//        double ret = cellToAdd.getNumericCellValue();
//	    if ((ret % 1) == 0) {
//	        if (doubleNumber > 0)
//	            return false;
//	        else return true;
//        }
//        else {
//	        if (intNumber > 0)
//	            return false;
//	        return true;
//        }
//    }



    private boolean diffSnippet(Cluster parentCluster, Cell childCell) {
        CellFeature feature1 = new CellFeature(null);
	    for (CellFeature cellFeature1:
             cellFeatureList) {
            if (cellFeature1.getCellReference().formatAsString().
                    equals(new CellReference(childCell).formatAsString())) {
                feature1 = cellFeature1;
                break;
            }
        }

        for (Cell cell1:
             parentCluster.getSeedCells()) {
            Snippet snippet1 = feature1.getSn();
            if (cell1.getRowIndex() >= snippet1.left && cell1.getRowIndex() <= snippet1.right &&
                    cell1.getColumnIndex() >= snippet1.up && cell1.getColumnIndex() <= snippet1.bottom) {
                return true;
            }
        }

        //TODO: now, I just consider the above string-header.

        int column = childCell.getColumnIndex();
        int row = childCell.getRowIndex()-1;
        while (row >= 0) {
            Row row1 = childCell.getSheet().getRow(row);
            if (row1 != null) {
                Cell cell1 = row1.getCell(column);
                if (cell1 != null && cell1.getCellType() == Cell.CELL_TYPE_STRING) {
                    Snippet snippet1 = feature1.getSn();
                    if (cell1.getRowIndex() >= snippet1.left && cell1.getRowIndex() <= snippet1.right &&
                            cell1.getColumnIndex() >= snippet1.up && cell1.getColumnIndex() <= snippet1.bottom) {
                        return false;
                    }
                }
            }
            row--;
        }

        if (row < 0)
            return false;


	    return true;
    }

    private boolean atLeastThree(Cluster seedCluster, Cell dCell) {
	    if (dCell.getCellType() != 0) //TODO: a formula cell
        {
            return false;
        }

	    int dRow = dCell.getRowIndex(), dColumn = dCell.getColumnIndex();

	    int referCount = 0;
	    //TODO: maybe we should try all formula cell's references, not just seed clusters
        //TODO: but don't know the overall influence

	    for (Cell fCell: seedCluster.getClusterCells()) {
            //TODO: check whether fCell references cell
            int fRow = fCell.getRowIndex(), fColumn = fCell.getColumnIndex();
            List<CellLocation> cellLocation = new FormulaParsing().getFormulaDependencies(sheetOrigin, fColumn, fRow);

            for (CellLocation cl : cellLocation) {
                if (cl.getRow() == dRow && cl.getColumn() == dColumn)
                    referCount++;
            }

            if (referCount >= 1)
                return true;
        }

	    return false;
    }

    private boolean checkConfidence(double maxValue, Cluster cluster,
			RealMatrix cellClusterMF, double threshold) {
		boolean check = true;
		
		List<Double> valueList = new ArrayList<Double>();
		List<CellReference> cellRefs = cluster.getSeedReference();

		double sum = 0;
		for (CellReference cr : cellRefs) {
			double data = cellClusterMF.getEntry(cellRefsVector.indexOf(cr),
					clusterVector.indexOf(cluster));
			valueList.add(data);
			sum += data;
		}

		double average = sum/cellRefs.size();

		double[] values = ArrayUtils.toPrimitive(valueList.toArray(new Double[valueList.size()]));
		
		Min min = new Min();

		double minimum = min.evaluate(values);

		//TODO: if cluster contains less than 4 cells
        double argument = 1;
        if (cluster.getClusterCellRefs().size() <= 3 && GP.plusTuning)
            argument = 0.97;

		if (maxValue == 0) {
			check = false;
		}
		else if (maxValue < minimum) {
			if ((minimum - maxValue) / minimum > threshold * argument)
				check = false;
		}
		//TODO: test whether the average can achieve better results
//        else if (maxValue < average) {
//		    if ((average - maxValue) / average > threshold)
//		        check = false;
//        }
//        System.out.printf("Average = %f, Minimum = %f, toHarvest = %f\n", average, minimum, maxValue);

        if (check) {
//		    out.printf("threshold * argument = %.6f, min = %.6f, (min - sim) / min = %.6f, ",
//                    threshold * argument, minimum, (minimum - maxValue) / minimum);
        }
		return check;
	}

	private boolean checkWithinClusterReference(Cluster cluster, CellReference cellRef, Cell cell) {
		List<Cell>           cells = cluster.getSeedCells();
		List<CellDependency> cds   = dependencyExtracting(cells);

		boolean accept = true;
		if (checkAll(cds)) {

			// Check this cell is not referenced by any other cell in the
			// cluster
			for (CellDependency cd : cds) {
				if (cd.getPrecedentCells() != null) {
					if (checkDependency(cd.precedentCells, cellRef)) {
						accept = false;
						break;
					}
				}
			}

			if (2 == cell.getCellType()) {
				// Check this cell is not referencing any other cell in the
				// cluster
				for (Cell cellClu : cells) {
					
					CellDependency cd = extractPrecedentCells(cell, true, true);

					if (cd != null) {
						List<CellReference> pre = cd.precedentCells;

						if (pre != null) {
							if (checkDependency(pre, new CellReference(cellClu))) {
								accept = false;
								break;
							}
						}
					}
				}
			}
		}

		return accept;
	}

	private CellDependency extractPrecedentCells(Cell cell, Boolean areaPtgExtract, Boolean refPtgExtract) {
		CellDependency cd = new CellDependency(cell);
		//String formula = cell.getCellFormula();
		String formula = new DataFormatter().formatCellValue(cell);

		//Only within worksheet
		if (!formula.contains("!") && !formula.startsWith("$")){
			try{
				Workbook wb = cell.getSheet().getWorkbook();
				Ptg[] fp = new FormulaParsing().getPtg(formula, wb, FormulaType.forInt(2), wb.getSheetIndex(cell.getSheet()));

				for (Ptg aFp : fp) {
					if (aFp.toString().contains("ptg.AreaPtg") && areaPtgExtract)
						extractCellsInAreaPtg(cell, cd.getPrecedentCells(), aFp);
					if (aFp.toString().contains("ptg.RefPtg") && refPtgExtract)
						extractCellInRefPtg(cd.getPrecedentCells(), aFp);
				}
				
			}
			catch(FormulaParseException e){
				e.printStackTrace();
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
		return cd;
	}
		
	private void extractCellsInAreaPtg(Cell curCell, List<CellReference> dependency, Ptg ptg){
			
		AreaPtg ar = new AreaPtg(ptg.toFormulaString());
		int firstRow = ar.getFirstRow();
		int lastRow = ar.getLastRow();
		int firstCol = ar.getFirstColumn();
		int lastCol = ar.getLastColumn();
					
					
		for (int ii = firstRow; ii < lastRow + 1; ii++) {
			Row r = curCell.getSheet().getRow(ii);
			if (r!=null){
				for (int j = firstCol; j < lastCol + 1; j++) {
					Cell cell = r.getCell(j);
					if (cell !=null){
						CellReference cellWithin = new CellReference(r.getCell(j));
						dependency.add(cellWithin);
					}
				}
			}
		}
	}
			
	private void extractCellInRefPtg(List<CellReference> dependency, Ptg ptg){
		CellReference rf = new CellReference(ptg.toFormulaString());
		dependency.add(rf);
	}

	private boolean checkDependency(List<CellReference> precedents, CellReference cellToCheck) {

		Boolean checkIfDepend = false;
		for (CellReference dependentCell: precedents){
			if (dependentCell.equals(cellToCheck)){
				checkIfDepend = true;
				break;
			}
		}
		return checkIfDepend;
	}

	private boolean checkAll(List<CellDependency> cds) {
		boolean dependentOrNot = true;
		
		for (int i=0; i< cds.size(); i++) {
			for (int j=0; j< cds.size(); j++) {
				if (i!=j) {
					CellDependency cdLeft = cds.get(i);
					CellDependency cdRight = cds.get(j);
					
					if (cdLeft.getPrecedentCells() != null) {
						if (checkDependency(cdLeft.getPrecedentCells(), new CellReference(cdRight.getCell()))) {
							dependentOrNot = false;
							break;
						}
					}
					
					if (cdRight.getPrecedentCells() != null) {
						if (checkDependency(cdRight.getPrecedentCells(), new CellReference(cdLeft.getCell()))) {
							dependentOrNot = false;
							break;
						}
					}
				}
			}
		}
		
		return dependentOrNot;
	}

	private List<CellDependency> dependencyExtracting(List<Cell> cells) {
		List<CellDependency> cds = new ArrayList<CellDependency>();

		for (Cell c : cells) {
			CellDependency cd = extractPrecedentCells(c, true, true);
			if (cd != null) {
				cds.add(cd);
			}
		}
		return cds;
	}
}
