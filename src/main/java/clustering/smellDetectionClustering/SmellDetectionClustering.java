package clustering.smellDetectionClustering;

import clustering.bootstrappingClustering.CellClusterMatrix;
import clustering.bootstrappingClustering.FeatureCellMatrix;
import clustering.bootstrappingClustering.FeatureClusterMatrix;
import entity.CellFeature;
import entity.Cluster;
import entity.FakeCell;
import entity.Smell;
import extraction.semllDetectionFeatureExtraction.TokenFeature;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.poi.ss.formula.FormulaParseException;
import org.apache.poi.ss.formula.FormulaType;
import org.apache.poi.ss.formula.ptg.AreaPtg;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.formula.ptg.RefPtg;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
import sun.security.ssl.Debug;
import utility.BasicUtility;
import utility.FormulaParsing;
import weka.classifiers.rules.DecisionTableHashKey;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SparseInstance;
import weka.filters.Filter;

import java.util.*;
import java.util.Map.Entry;

import static datasets.TestEUSES.TIMEOUT;
import static kernel.GP.addA;
import static kernel.GP.addB;
import static kernel.GP.addC;

public class SmellDetectionClustering {

	private Sheet sheet;
	private List<Cluster> clusters;
	private List<CellFeature> fts;
	private List<Smell> detectedSmellyCells = new ArrayList<>();
	private BasicUtility bu = new BasicUtility();

	private List<String> featureVector = null;
	private List<CellFeature> subFtList = null;
    private Debug out = new Debug();

    private long timeout = (long) (TIMEOUT * 1_000_000_000.0);
    private long beginTime;
    private volatile Thread blinker;
	
	public SmellDetectionClustering(Thread blinker, Sheet sheet, List<Cluster> clusters, List<CellFeature> fts, long beginTime) {
		this.blinker = blinker;
	    this.sheet = sheet;
		this.clusters = clusters;
		this.fts = fts;
		this.beginTime = beginTime;
	}

	public void outlierDetection() throws Exception {
		for (Cluster cl : clusters) {
            if (blinker != Thread.currentThread()) {
                throw new RuntimeException();
            }

			Map<CellReference, Double> cellRefsValue = cl.cellRefsWithValue();

			List<Cell> formulaInCluster = new ArrayList<Cell>();
			List<CellReference> formulaRefInCluster = new ArrayList<CellReference>();

//			List<Cell> dataInCluster = new ArrayList<>();
//			List<CellReference> dataRefInCluster = new ArrayList<>();

			for (Entry<CellReference, Double> entry : cellRefsValue.entrySet()) {
				CellReference cr = entry.getKey();
				Cell cell = sheet.getRow(cr.getRow()).getCell(cr.getCol());

				if (cell.getCellType() == Cell.CELL_TYPE_FORMULA) {
					formulaInCluster.add(cell);
					formulaRefInCluster.add(cr);
				}
			}

            if (addC)
                filterDataCellsByReplace(cl);

            if (addB)
                filterDataCellsByOverlap(cl);

            if (addA) {
                double coverageRate = coverageInFormulas(formulaInCluster);
                cl.coverage = coverageRate;
                if (coverageRate < 0.5) {
                    continue;
                }
            }

			//TODO: 直接把和占多数的公式形式不同的formula cell直接标记为smell 效果会不会更好
            //尚未实现
			
			detectionFeatureExtraction(sheet.getWorkbook(), formulaInCluster);
			FeatureCellMatrix fc = new FeatureCellMatrix(blinker, featureVector, formulaRefInCluster, beginTime);
			RealMatrix fcM = fc.matrixCreationForDetection(subFtList);
			
			Instances originalDataset = createInstances(fcM);
			Instances outliers;



			if (originalDataset.size() == 3) {
				//这里似乎有点问题，对于仅有3个formula cells的类型 似乎没有做任何处理
			    twoUniqueInstancesHandling(originalDataset);
				/*
                for (Cell cell1: formulaInCluster) {
                    for (Cell cell2: formulaInCluster) {
                        if (!cell1.equals(cell2)) {
                            BasicUtility bu = new BasicUtility();
                            String r1c1Formula1 = bu.convertA1ToR1C1(cell1.getRowIndex(), cell1.getColumnIndex(),
                                    cell1.getCellFormula());
                            String r1c1Formula2 = bu.convertA1ToR1C1(cell2.getRowIndex(), cell2.getColumnIndex(),
                                    cell2.getCellFormula());
                            if (r1c1Formula1.equals(r1c1Formula2)) {
                                if (!correctFormulaList.contains(cell1)) correctFormulaList.add(cell1);
                                if (!correctFormulaList.contains(cell2)) correctFormulaList.add(cell2);
                            }
                        }
                    }
                }
                */
			}
			else if (originalDataset.size() > 3) {
				outliers = LofAnalysis(originalDataset);
				reportAll(outliers, formulaRefInCluster, fcM);
			}

            //TODO: 1.先考虑公式的覆盖率 如果满足合适的约束 才能够将这个类中的data cells标记为defects
            //TODO: 2.（尚未实现的想法）其实cluster内部的公式也有一个覆盖率的问题，如果很多公式都不能相容，整个类都可以舍弃

            //tackleDataCells(cl, correctFormulaList);

            //把剩余的data cells标记为smell
            for (Cell cell:
                    cl.getClusterCells()) {
                if (cell.getCellType() == 0) {
                    CellReference cr = new CellReference(cell);
                    Smell smell = new Smell(cr);
                    smell.isMissingFormulaSmell = true;
                    detectedSmellyCells.add(smell);
                }
            }
		}

        for (int i = clusters.size()-1; i>=0; i--) {
		    Cluster cl = clusters.get(i);
            if (cl.coverage < 0.5) {
                clusters.remove(cl);
            }
        }
	}


    /*
    1: 统计1项预定义指标：引用是否重叠。
    2: 看这项指标是否每次都为空，或非空，否则该指标失效。(这是一个hard-rule)
    3: 针对所有的cells，任取两个cells(其中必须含有至少一个data cell)，比较引用是否重叠，和step 2中的统计结果对比，如果不一致，
       那么就认定这个data cell(s)需要被剔除。
    Note:另外需要注意，整个操作必须保留所有data cells进行全体判断，不能先判断先删除，否则会影响其他cells的判断结果。
     */
	private void filterDataCellsByOverlap(Cluster cluster) {
	    // step 1
        //out.println("begin Filter On overlap");

	    int flagInSeed = 0;

	    List<Cell> seedCells = cluster.getSeedCells();
        for (int i = 0; i < seedCells.size(); i++) {
            Cell cell_i = seedCells.get(i);
            List<FakeCell> fakeCellList_i = getFakeCellList(cell_i.getCellFormula());

            for (int j = i+1; j < seedCells.size(); j++) {
                Cell cell_j = seedCells.get(j);
                List<FakeCell> fakeCellList_j = getFakeCellList(cell_j.getCellFormula());

                boolean plusFlag = false;
                for (FakeCell fc: fakeCellList_i) {
                    if (fakeCellList_j.contains(fc)) {
                        flagInSeed++;
                        plusFlag = true;
                        break;
                    }
                }

                //(ref 和 ref 的重叠) 或 (ref 和 cell 是否重叠)
                if (plusFlag) continue;

                FakeCell fakeCellI = new FakeCell(cell_i);
                FakeCell fakeCellJ = new FakeCell(cell_j);

                if (fakeCellList_i.contains(fakeCellJ)) flagInSeed++;
                else if (fakeCellList_j.contains(fakeCellI)) flagInSeed++;
            }
        }


        // step 2
        // @args flagInSeed=0: cannot overlap
        // @args flagInSeed=1: must overlap
        // @args flagInSeed=others: invalid
        int maximum = seedCells.size()*(seedCells.size()-1)/2;
        if (flagInSeed == 0) flagInSeed = 0;
        else if (flagInSeed == maximum) flagInSeed = 1;
        else return;


	    // step 3
        BasicUtility basicUtil = new BasicUtility();
        List<Cell> cells = cluster.getClusterCells();
        List<Boolean> deleteList = new ArrayList<>();
        for (int i = 0; i < cells.size(); i++) {
            deleteList.add(false);
        }

        for (int i = 0; i < cells.size(); i++) {
            Cell cell_i = cells.get(i);
            if (cell_i.getCellType() != Cell.CELL_TYPE_NUMERIC) continue;

            //out.println("cellI = " + new CellReference(cell_i).formatAsString());
            //枚举每个seed cell的公式来替换之,注意加入删去重复的R1C1的公式格
            Set<String> visitedFormulaSet = new HashSet<>();
            boolean existOne = false;

            for (int k = 0; k < seedCells.size(); k++) {
                Cell refCell = seedCells.get(k);
                String r1c1F = bu.convertA1ToR1C1(refCell.getRowIndex(), refCell.getColumnIndex(),
                        refCell.getCellFormula());
                if (visitedFormulaSet.contains(r1c1F)) continue;

                visitedFormulaSet.add(r1c1F);
                List<FakeCell> fakeCellList_i = getDataFakeCellList(cell_i, refCell);
                if (fakeCellList_i == null) continue;

                //下面的逻辑写错了,对给定的d(i)和f(k)
                boolean flagForAll = true;
                for (Cell cell_j : cells) {
                    int indexI = cells.indexOf(cell_i);
                    int indexJ = cells.indexOf(cell_j);
                    if (indexI == indexJ) continue;

                    List<FakeCell> fakeCellList_j;
                    if (cell_j.getCellType() == Cell.CELL_TYPE_FORMULA) {
                        fakeCellList_j = getFakeCellList(cell_j.getCellFormula());

                        //标志位
                        boolean ret = satisfyRR(fakeCellList_i, fakeCellList_j, flagInSeed);

                        //out.println("ret = " + ret);
                        if (!ret) flagForAll = false;
                    } else {
                        boolean innerExistOne = false;
                        Set<String> innerVisitedFormulaSet = new HashSet<>();

                        for (Cell innerRefCell : seedCells) {
                            String innerR1C1F = bu.convertA1ToR1C1(innerRefCell.getRowIndex(),
                                    innerRefCell.getColumnIndex(), innerRefCell.getCellFormula());
                            if (innerVisitedFormulaSet.contains(innerR1C1F)) continue;

                            innerVisitedFormulaSet.add(innerR1C1F);
                            fakeCellList_j = getDataFakeCellList(cell_j, innerRefCell);
                            if (fakeCellList_j == null) continue;

                            boolean flag1 = satisfyRR(fakeCellList_i, fakeCellList_j, flagInSeed);
                            boolean flag2 = satisfyRC(fakeCellList_i, fakeCellList_j, cell_i, cell_j, flagInSeed);

                            if (flag1 && flag2) {
                                innerExistOne = true;
                                break;
                            }
                        }

                        //out.println("innerExistOne = " + innerExistOne);
                        if (!innerExistOne) flagForAll = false;
                    }
                }

                //out.println("flagForAll = " + flagForAll);
                if (flagForAll) existOne = true;
            }

            //out.println("existOne = " + existOne);
            if (!existOne) deleteList.set(i, true);
        }

        //out.println("begin to delete cells");
        deleteDataCells(deleteList, cluster);

        //out.println("------");
    }

    private boolean satisfyRC(List<FakeCell> fakeCellList_i, List<FakeCell> fakeCellList_j,
                              Cell cell_i, Cell cell_j, int flagInSeed) {
	    boolean ret = true;

	    FakeCell fakeCellI = new FakeCell(cell_i);
	    FakeCell fakeCellJ = new FakeCell(cell_j);
	    boolean inFlag = false;
	    if (fakeCellList_i.contains(fakeCellJ) || fakeCellList_j.contains(fakeCellI)) inFlag = true;

	    if ((flagInSeed == 0 && inFlag) || (flagInSeed == 1 && !inFlag))
	        ret = false;

	    return ret;
    }

    private boolean satisfyRR(List<FakeCell> fakeCellList_i, List<FakeCell> fakeCellList_j, int flagInSeed) {
	    boolean ret = true;

        for (FakeCell fakeCell: fakeCellList_i) {
            boolean containFlag = fakeCellList_j.contains(fakeCell);
            if ((flagInSeed == 0 && containFlag) || (flagInSeed == 1 && !containFlag))
                ret = false;
        }

        return ret;
    }


    private List<FakeCell> getDataFakeCellList(Cell cell, Cell refCell) {

        //TODO:让我修改一下获取数值格子的引用方法 不能用refCell来间接算,干脆直接替换成公式然后重新算
        //@step 先把数值格替换成公式格
        int relativeRow = cell.getRowIndex() - refCell.getRowIndex();
        int relativeColumn = cell.getColumnIndex() - refCell.getColumnIndex();
        String newFormulaS = new BasicUtility().convertA1ToA1(relativeRow, relativeColumn, refCell.getCellFormula());

        //@step 直接extract新产生的伪装公式格的引用集合
        List<FakeCell> fakeCellList = getFakeCellList(newFormulaS);

        //@step 返回引用集合 并且把伪装公式格替换回去

        return fakeCellList;
    }

    private List<FakeCell> getFakeCellList(String cellFormula) {
        Workbook workbook = sheet.getWorkbook();
        int sheetIndex = workbook.getSheetIndex(sheet);
        Ptg[] ptgList = new FormulaParsing().getPtg(cellFormula, workbook, FormulaType.forInt(2), sheetIndex);

        if (ptgList == null) return null;

        List<FakeCell> fakeCellList = new ArrayList<>();

        for (Ptg aPtg:
                ptgList) {
            String ptgString = aPtg.toString();

            if (ptgString.contains("RefPtg")) {
                RefPtg exactPtg = new RefPtg(aPtg.toFormulaString());
                int rowPtg = exactPtg.getRow();
                int columnPtg = exactPtg.getColumn();
                fakeCellList.add(new FakeCell(rowPtg, columnPtg));
            }
            else if (ptgString.contains("AreaPtg")) {
                AreaPtg exactPtg = new AreaPtg(aPtg.toFormulaString());
                int firstRow = exactPtg.getFirstRow();
                int lastRow = exactPtg.getLastRow();
                int firstCol = exactPtg.getFirstColumn();
                int lastCol = exactPtg.getLastColumn();

                for (int i = firstRow; i <= lastRow; i++) {
                    for (int j = firstCol; j <= lastCol; j++) {
                        fakeCellList.add(new FakeCell(i, j));
                    }
                }
            }
        }

        return fakeCellList;
    }

    private void filterDataCellsByReplace(Cluster cluster) {
        //这个判断其实有冗余部分，可优化。
        List<Boolean> deleteList = new ArrayList<>();
        List<Cell> cells = cluster.getClusterCells();
        for (int i = 0; i < cells.size(); i++) deleteList.add(false);

        for (Cell cell : cluster.getClusterCells()) {
            if (cell.getCellType() != Cell.CELL_TYPE_NUMERIC) continue;
            int index = cells.indexOf(cell);

            boolean existOne = false;
            //NOTE:其实这里只选择seed (formula) cells是一个很tricky的地方
            for (Cell refCell : cluster.getSeedCells()) {
                boolean compFlag = compatible(cell, refCell, sheet);
                if (compFlag) existOne = true;
            }

            if (!existOne) deleteList.set(index, true);
        }

        deleteDataCells(deleteList, cluster);
    }

    private void deleteDataCells(List<Boolean> deleteList, Cluster cluster) {
	    List<Cell> cells = cluster.getClusterCells();

	    //NOTE: 这里一定要倒序处理,否则会发生OutOfBoundException.
        for (int i = deleteList.size()-1; i > 0; i--) {
            if (deleteList.get(i)) {
                cluster.removeChild(cells.get(i));
            }
        }
    }

    private int stringToInt(String columnS) {
        int ret = 0;
        for (int index = 0; index < columnS.length(); index++) {
            ret = ret*26 + columnS.charAt(index)-'A';
        }
        return ret;
    }

    //i.e., type inference
    private boolean compatible(Cell cell, Cell refCell, Sheet sheet) {
        int relativeRow = cell.getRowIndex() - refCell.getRowIndex();
        int relativeColumn = cell.getColumnIndex() - refCell.getColumnIndex();
        String newFormulaS = new BasicUtility().convertA1ToA1(relativeRow, relativeColumn, refCell.getCellFormula());

        Workbook workbook = sheet.getWorkbook();
        int sheetIndex = workbook.getSheetIndex(sheet);
        Ptg[] ptgList = new FormulaParsing().getPtg(newFormulaS, workbook, FormulaType.forInt(2), sheetIndex);

        if (ptgList == null) return false;

        boolean ret = true;
        for (Ptg ptg: ptgList) {
            String ptgS = ptg.toString();

            if (ptgS.contains("RefPtg")) {
                RefPtg refPtg = new RefPtg(ptg.toFormulaString());
                FakeCell fakeCell = new FakeCell(refPtg.getRow(), refPtg.getColumn());
                boolean flag = compatibleInCell(fakeCell);
                if (!flag) {
                    ret = false;
                    break;
                }
            }
            else if (ptgS.contains("AreaPtg")) {
                AreaPtg areaPtg = new AreaPtg(ptg.toFormulaString());
                for (int rowIndex = areaPtg.getFirstRow(); rowIndex <= areaPtg.getLastRow(); rowIndex++) {
                    int lastColumn = areaPtg.getLastColumn();
                    for (int columnIndex = areaPtg.getFirstColumn(); columnIndex <= lastColumn; columnIndex++) {
                        FakeCell fakeCell = new FakeCell(rowIndex, columnIndex);
                        boolean flag = compatibleInCell(fakeCell);
                        if (!flag) {
                            ret = false;
                            break;
                        }
                    }
                }
            }
        }

        return ret;
    }

    private boolean compatibleInCell(FakeCell fakeCell) {
	    boolean ret;

	    Row row = sheet.getRow(fakeCell.getRow());
	    if (row == null) return false;
	    Cell cell = row.getCell(fakeCell.getColumn());
	    if (cell == null) return false;

	    switch (cell.getCellTypeEnum()) {
            case NUMERIC:
                ret = true;
                break;
            case FORMULA:
                ret = true;
                break;
            case BLANK:
                ret = true;
                break;
            default:
                ret = false;
                break;
        }

	    return ret;
    }

    //Cluster only contains formula cells in a given cluster
	private double coverageInFormulas(List<Cell> cellList) {
	    int ret = 0;
        BasicUtility basicUtil = new BasicUtility();
        FormulaEvaluator evaluator = sheet.getWorkbook().getCreationHelper().createFormulaEvaluator();

        List<Double> originalValues = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < cellList.size(); i++) {
            originalValues.add(0.0);
            Cell cell = cellList.get(i);
            try {
                originalValues.set(i, cell.getNumericCellValue());
            }
            catch (IllegalStateException ise) {
                originalValues.set(i, random.nextDouble());
            }
        }

		for (int i=0; i<cellList.size(); i++) {
		    //evaluator.evaluateAll();
		    int thisCov = 0;

			Cell cLeft = cellList.get(i);
			String rcLeft = bu.convertA1ToR1C1(cLeft.getRowIndex(), cLeft.getColumnIndex(), cLeft.getCellFormula());
			//System.out.println("detect cell:" + new CellReference(cLeft).formatAsString() + ", R1C1 formula = " + rcLeft);

			for (int j=0; j<cellList.size(); j++) {
				Cell cRight = cellList.get(j);
				String rcRight = bu.convertA1ToR1C1(cRight.getRowIndex(), cRight.getColumnIndex(), cRight.getCellFormula());

				if (rcLeft.equals(rcRight)) {
					thisCov++;
				}
				else {
                    String originalS = cRight.getCellFormula();
                    if (originalS == null || originalS.contains("!")) continue;

                    //参考Cluster.checkWeakCoverage方法
                    double originalValue = originalValues.get(j);

                    int relativeRow = cRight.getRowIndex() - cLeft.getRowIndex();
                    int relativeColumn = cRight.getColumnIndex() - cLeft.getColumnIndex();
                    String newFormulaS = basicUtil.convertA1ToA1(relativeRow, relativeColumn, cLeft.getCellFormula());
//                    System.out.println("OriginalS = " + originalS +
//                            ", generated A1 formula = " + newFormulaS + ", thisCov = " + thisCov);

                    if (newFormulaS == null || newFormulaS.contains("!")) continue;

                    //标志位
                    boolean errors = false;
                    evaluator.clearAllCachedResultValues();
                    try {
                        cRight.setCellFormula(newFormulaS);
                    }
                    catch (FormulaParseException e) {
                        //恢复原来的公式
                        evaluator.clearAllCachedResultValues();
                        cRight.setCellFormula(originalS);
                        try {
                            evaluator.evaluateFormulaCell(cRight);
                        }
                        catch (RuntimeException re) {
                            errors = true;
                        }

                        errors = true;
                    }
                    catch (IllegalStateException ignored) {
                        System.out.println("Cell H10 is part of a multi-cell array formula. " +
                                "You cannot change part of an array.");
                        errors = true;
                    }

                    if (errors) continue;

                    double generatedValue = 0;
                    try {
                        evaluator.evaluateFormulaCell(cRight);
                        generatedValue = cRight.getNumericCellValue();
                    } catch (RuntimeException re) {
                        errors = true;
                    }

                    //恢复原来的公式
                    evaluator.clearAllCachedResultValues();
                    cRight.setCellFormula(originalS);
                    try {
                        evaluator.evaluateFormulaCell(cRight);
                    }
                    catch (RuntimeException e) {
                        errors = true;
                    }

                    if (errors) continue;

                    if (originalValue == generatedValue) thisCov++;
//                    System.out.println("originalValue = " + originalValue + ", generatedValue = " + generatedValue);
                }
			}

			if (ret < thisCov) {
			    ret = thisCov;
            }

//            System.out.println();
		}
		
		return 1.0*ret/cellList.size();
	}

	private List<Cell> reportAll(Instances dataset, List<CellReference> cellRefList, RealMatrix featureCellM) {
		boolean[] taggedCells = new boolean[cellRefList.size()];
		List<CellReference> correctRefList = new ArrayList<>();
		Boolean outlierExist = false;
		Boolean inlierExist = false;

		for (int i = 0; i < dataset.size(); i++)
			if (dataset.get(i).value(dataset.numAttributes() - 1) > 0.0001) {
				// True for outlier in taggedCells
				taggedCells[i] = true;
				outlierExist = true;
			} else {
		        correctRefList.add(cellRefList.get(i));
				taggedCells[i] = false;
				inlierExist = true;
			}

        List<Smell> formulaSmellList;
		if (outlierExist && inlierExist) {
		    formulaSmellList = smellDescriptionDetection(taggedCells, featureCellM, cellRefList);
			detectedSmellyCells.addAll(formulaSmellList);
		}

		List<Cell> correctList = new ArrayList<>();
        for (CellReference cr:
             correctRefList) {
            Cell cell = sheet.getRow(cr.getRow()).getCell(cr.getCol());
            correctList.add(cell);
        }

        return correctList;
    }

	private List<Smell> smellDescriptionDetection(boolean[] taggedCells,
			RealMatrix featureCellM, List<CellReference> cellRefsVector) {
		List<Smell> smells = new ArrayList<>();

		CellClusterMatrix cc = new CellClusterMatrix();
		RealMatrix lierMatrix = cc.matrixCreation(taggedCells);

		FeatureClusterMatrix fcm = new FeatureClusterMatrix(featureCellM,
				lierMatrix);
		RealMatrix featureLierM = fcm.matrixCreation();

		featureLierM = fcm.computeNPMI(featureLierM);
		double[] inlierFeatureAssociation = featureLierM.getColumn(0);
		int[] sigFeatureSet = new int[inlierFeatureAssociation.length];

		for (int i = 0; i < inlierFeatureAssociation.length; i++) {
			if (inlierFeatureAssociation[i] > 0) {
				sigFeatureSet[i] = 1;
			}
		}

		for (int i = 0; i < cellRefsVector.size(); i++) {
			if (taggedCells[i]) {
				CellReference cr = cellRefsVector.get(i);
				Smell smell = new Smell(cr);

				RealMatrix cellFtM = featureCellM
						.getColumnMatrix(cellRefsVector.indexOf(cr));

				for (int i1 = 0; i1 < sigFeatureSet.length; i1++) {
					if (cellFtM.getEntry(i1, 0) == 0 && sigFeatureSet[i1] == 1) {
						String description = featureVector.get(i1);

						smell.isDissimilarFormulaSmell = true;

						if (description.contains("Operation=")) {
							smell.isDissimilarOperationSmell = true;
						} else if (description.contains("ScalarConstantPtg")) {
							smell.isHardCodedConstantSmell = true;
						} else if (description.contains("RefToken")) {
							smell.isDissimilarCellReferenceSmell = true;
						}
					}
				}
				smells.add(smell);
			}
		}

		return smells;
	}

	private Instances LofAnalysis(Instances dataset) throws Exception {
		LOF lof = new LOF();
		String minPotsLowerBound = Integer.toString((int) Math.floor(Math
				.sqrt(dataset.size())));
		String minPotsUpperBound = Integer.toString((int) Math.floor(Math
				.sqrt(dataset.size())));
		lof.setMinPointsLowerBound(minPotsLowerBound);
		lof.setMinPointsUpperBound(minPotsUpperBound);
		lof.setInputFormat(dataset);
		dataset = Filter.useFilter(dataset, lof);
		return dataset;
	}

	private Instances twoUniqueInstancesHandling(Instances originalDataset) throws Exception {
		Instances outliers;

		Map<DecisionTableHashKey, Integer> twoInst = new HashMap<>();
		for (Instance inst : originalDataset) {
			DecisionTableHashKey key = new DecisionTableHashKey(inst,
					originalDataset.numAttributes(), true);
			if (!twoInst.containsKey(key)) {
				twoInst.put(key, 0);
			}
			twoInst.put(key, twoInst.get(key) + 1);
		}
		outliers = new Instances(originalDataset);
		outliers.insertAttributeAt(new Attribute("LOF"),
				outliers.numAttributes());

		for (int j = 0; j < outliers.numInstances(); j++) {
			Instance instN = outliers.get(j);
			for (Entry<DecisionTableHashKey, Integer> entry : twoInst
					.entrySet()) {
				if (new DecisionTableHashKey(instN,
						originalDataset.numAttributes(), true).equals(entry.getKey())) {
					double value = 1 - (double) entry.getValue()
							/ (double) originalDataset.size();
					instN.setValue(outliers.numAttributes() - 1, value);
				}
			}
		}
		return outliers;
	}

	private Instances createInstances(RealMatrix subFeatureCellMatrix) {
		double data[][] = subFeatureCellMatrix.getData();
		ArrayList<Attribute> attributes = new ArrayList<>();
		List<Instance> instances = new ArrayList<>();

		for (int obj = 0; obj < subFeatureCellMatrix.getColumnDimension(); obj++) {
			instances.add(new SparseInstance(subFeatureCellMatrix
					.getRowDimension()));

		}

		for (int dim = 0; dim < subFeatureCellMatrix.getRowDimension(); dim++) {

			// Create new attribute / dimension
			Attribute current = new Attribute("Attribute" + dim, dim);

			// Fill the value of dimension "dim" into each object
			for (int obj = 0; obj < subFeatureCellMatrix.getColumnDimension(); obj++) {
				instances.get(obj).setValue(current, data[dim][obj]);
			}
			// Add attribute to total attributes
			attributes.add(current);
		}

		Instances dataset = new Instances("Dataset", attributes, instances.size());

        dataset.addAll(instances);

		return new Instances(dataset);
	}

	private void detectionFeatureExtraction(Workbook wb, List<Cell> cells) {
		subFtList = new ArrayList<>();
		featureVector = new ArrayList<>();
		for (Cell cell : cells) {
			CellReference cr = new CellReference(cell);
			CellFeature ft = new CellFeature(cr);
			CellFeature ftInList = fts.get(fts.indexOf(ft));
			TokenFeature tokenF = new TokenFeature(cell, wb);
			List<List<String>> tokens = tokenF.getFeature();

			if (tokens != null && !tokens.isEmpty()) {

				ftInList.setTokens(tokenF.getFeature());

				List<String> opTokens = tokens.get(0);

				if (opTokens != null) {
					ftInList.setOpTokens(opTokens);
					StringBuilder ops = new StringBuilder();
					for (String opToken : opTokens) {
						ops.append(opToken).append(",");
					}
					if (!featureVector.contains("Operation=" + ops)) {
						featureVector.add("Operation=" + ops);
					}
				}

				List<String> refTokens = tokens.get(1);
				if (refTokens != null && !refTokens.isEmpty()) {
					ftInList.setRefTokens(refTokens);
					StringBuilder ref = new StringBuilder();
					for (String refToken : refTokens) {
						ref.append(refToken).append(",");
					}
					if (!featureVector.contains("RefToken" + ref)) {
						featureVector.add("RefToken" + ref);
					}

				}
				List<String> scalarToken = tokens.get(2);
				if (!featureVector.contains("ScalarConstantPtg")) {
					featureVector.add("ScalarConstantPtg");
				}
				if (scalarToken != null && !scalarToken.isEmpty()) {
					ftInList.setScalarInFormula(true);
				}
			}
			subFtList.add(ftInList);
		}
	}

	public Sheet getSheet() {
		return sheet;
	}

	public void setSheet(Sheet sheet) {
		this.sheet = sheet;
	}

	public List<Smell> getDetectedSmellyCells() {
		return detectedSmellyCells;
	}

}
