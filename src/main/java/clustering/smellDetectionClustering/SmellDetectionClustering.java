package clustering.smellDetectionClustering;

import clustering.bootstrappingClustering.CellClusterMatrix;
import clustering.bootstrappingClustering.FeatureCellMatrix;
import clustering.bootstrappingClustering.FeatureClusterMatrix;
import org.apache.poi.ss.formula.FormulaType;
import org.apache.poi.ss.formula.ptg.AreaPtg;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.formula.ptg.RefPtg;
import utility.BasicUtility;
import entity.CellFeature;
import entity.Cluster;
import featureExtraction.semllDetectionFeatureExtraction.TokenFeature;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;
import utility.FormulaParsing;
import weka.classifiers.rules.DecisionTableHashKey;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SparseInstance;
import weka.filters.Filter;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class SmellDetectionClustering {

	private Sheet sheet = null;
	private List<Cluster> clusters = null;
	private List<CellFeature> fts = null;
	private List<Smell> detectedSmellyCells = new ArrayList<Smell>();
	private BasicUtility bu = new BasicUtility();

	private List<String> featureVector = null;
	private List<CellFeature> subFtList = null;
	
	public SmellDetectionClustering(Sheet sheet, List<Cluster> clusters, List<CellFeature> fts) {
		this.sheet = sheet;
		this.clusters = clusters;
		this.fts = fts;
	}

	public void outlierDetection() throws Exception {
		for (Cluster cl : clusters) {
			Map<CellReference, Double> cellRefsValue = cl.cellRefsWithValue();

			List<Cell> formulaInCluster = new ArrayList<Cell>();
			List<CellReference> formulaRefInCluster = new ArrayList<CellReference>();

			List<Cell> dataInCluster = new ArrayList<>();
			List<CellReference> dataRefInCluster = new ArrayList<>();

			for (Entry<CellReference, Double> entry : cellRefsValue.entrySet()) {
				CellReference cr = entry.getKey();
				Cell cell = sheet.getRow(cr.getRow()).getCell(cr.getCol());
				if (cell.getCellType() == 0) {
					dataInCluster.add(cell);
					dataRefInCluster.add(cr);
//                    filterDataCells(dataInCluster, dataRefInCluster, cl);
//					Smell sl = new Smell(cr);
//					sl.isMissingFormulaSmell = true;
//					detectedSmellyCells.add(sl);
				} else if (cell.getCellType() == 2) {
					formulaInCluster.add(cell);
					formulaRefInCluster.add(cr);
				}
			}

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

			if (checkAllTheSame(formulaInCluster)) {
				continue;
			}
			
			detectionFeatureExtraction(sheet.getWorkbook(), formulaInCluster);
			
			FeatureCellMatrix fc = new FeatureCellMatrix(featureVector, formulaRefInCluster);
			RealMatrix fcM = fc.matrixCreationForDetection(subFtList);
			
			Instances originalDataset = createInstances(fcM);
			Instances outliers;
			
			if (originalDataset.size() == 3) {
				outliers = twoUniqueInstancesHandling(originalDataset);
			}
			else if (originalDataset.size() > 3) { 
				outliers = LofAnalysis(originalDataset);
				reportAll(outliers, formulaRefInCluster, fcM);
			}
		}
	}

    /* todo
    1: 统计2项预定义指标（追对相邻的两个cells）：引用是否重叠；其中一个是否属于另一个的引用集合。
    2: 总体看是否2项指标每次都为空，或非空，否则该指标失效。（这是一个hard-rule，一刀切，我没有进行调参）
    3: 针对所有的cells，对于任意两个cells(其中必须含有至少一个data cell)，同样统计2项指标，和2中统计结果对比，如果不一致，
       那么就认定这个data cell(s)需要被剔除。
     */
	private void filterDataCells(List<Cell> datum, List<CellReference> dataRefs, Cluster cluster) {
	    // step 1
	    int RRCountInSeed = 0,  RCCountInSeed = 0;

	    List<Cell> seedCells = cluster.getSeedCells();
        for (int i = 0; i < seedCells.size(); i++) {
            Cell cell_i = seedCells.get(i);
            FakeCell fakeCell_i = new FakeCell(cell_i.getRowIndex(), cell_i.getColumnIndex());
            List<FakeCell> fakeCellList_i = getFakeCellList(cell_i);

            for (int j = i+1; j < seedCells.size(); j++) {
                Cell cell_j = seedCells.get(j);
                FakeCell fakeCell_j = new FakeCell(cell_j.getRowIndex(), cell_j.getColumnIndex());
                List<FakeCell> fakeCellList_j = getFakeCellList(cell_j);

                for (FakeCell fc:
                     fakeCellList_i) {
                    if (fakeCellList_j.contains(fc))
                        RRCountInSeed++;
                }
                if (fakeCellList_j.contains(fakeCell_i) || fakeCellList_i.contains(fakeCell_j))
                    RCCountInSeed++;
            }
        }

        // step 2
        // 0: no overlapping 1: must overlap 2: undecidable
        int maximum = seedCells.size()*(seedCells.size()-1)/2;
        if (RRCountInSeed == 0)
            RRCountInSeed = 0;
        else if (RRCountInSeed == maximum)
            RRCountInSeed = 1;
        else
            RRCountInSeed = 2;

        if (RCCountInSeed == 0)
            RCCountInSeed = 0;
        else if (RCCountInSeed == maximum)
            RCCountInSeed = 1;
        else
            RCCountInSeed = 2;

	    // step 3
        Map<String, Integer> formulaCount = new HashMap<>();
        for (Cell cell:
             seedCells) {
            String key = cell.getCellFormula();
            if (formulaCount.containsKey(key)) {
                int value = formulaCount.get(key);
                formulaCount.remove(key);
                formulaCount.put(key, value+1);
            }
            else {
                formulaCount.put(key, 1);
            }
        }

        String formula = null;
        int count = 0;
        for (Entry<String, Integer> map:
             formulaCount.entrySet()) {
            if (map.getValue() > count) {
                count = map.getValue();
                formula = map.getKey();
            }
        }

        //先搞个强行停止的算法，回头再细想。这本质上是当多个formula数量都排第一该怎么办。
        if (count == 1)
            return;

        Cell refCell = null;
        for (Cell cell:
             seedCells) {
            if (cell.getCellFormula().equals(formula)) {
                refCell = cell;
                break;
            }
        }

        //Careful: 目前都是只考虑相对引用，对于绝对引用还是要修改最底层的算法。

        List<Cell> cells = cluster.getClusterCells();
        List<Boolean> deleteList = new ArrayList<>();
        for (int i = 0; i < cells.size(); i++) {
            deleteList.add(false);
        }

        for (int i = 0; i < cells.size(); i++) {
            Cell cell_i = cells.get(i);
            FakeCell fakeCell_i = new FakeCell(cell_i.getRowIndex(), cell_i.getColumnIndex());
            List<FakeCell> fakeCellList_i = getDataFakeCellList(cell_i, refCell);

            for (int j = i+1; j < cells.size(); j++) {
                Cell cell_j = cells.get(j);
                FakeCell fakeCell_j = new FakeCell(cell_j.getRowIndex(), cell_j.getColumnIndex());
                List<FakeCell> fakeCellList_j = getDataFakeCellList(cell_j, refCell);

                boolean flag = false;
                //RR
                for (FakeCell fakeCell:
                     fakeCellList_i) {
                    if (RRCountInSeed == 0 && fakeCellList_j.contains(fakeCell))
                        flag = true;
                    if (RRCountInSeed == 1 && !fakeCellList_j.contains(fakeCell))
                        flag = true;
                }

                //RC 感觉有点怪怪的，似乎bug了
                if (RCCountInSeed == 0 && (fakeCellList_i.contains(fakeCell_j) || fakeCellList_j.contains(fakeCell_i)))
                    flag = true;
                if (RCCountInSeed == 1 && (!fakeCellList_i.contains(fakeCell_j) || !fakeCellList_j.contains(fakeCell_i)))
                    flag = true;

                if (flag) {
                    //删除这两个cells中的data cell。
                    if (cell_i.getCellType() == 0)
                        deleteList.set(i, true);
                    if (cell_j.getCellType() == 0)
                        deleteList.set(j, true);
                }
            }
        }

        for (int i = deleteList.size()-1; i > 0; i--) {
            if (deleteList.get(i)) {
                cluster.removeChild(cells.get(i));
            }
        }

    }

    private List<FakeCell> getDataFakeCellList(Cell cell, Cell refCell) {
        List<FakeCell> fakeCellList = new ArrayList<>();
        if (cell.getCellType() == 0) {
            List<FakeCell> refList = getFakeCellList(refCell);
            for (FakeCell fakeCell:
                 refList) {
                int row = cell.getRowIndex() - (refCell.getRowIndex() - fakeCell.row);
                int column = cell.getColumnIndex() - (refCell.getColumnIndex() - fakeCell.column);
                fakeCellList.add(new FakeCell(row, column));
            }
        }
        else if (cell.getCellType() == 2) {
            fakeCellList = getFakeCellList(cell);
        }

        return fakeCellList;
    }

    private List<FakeCell> getFakeCellList(Cell cell) {
        Workbook workbook = sheet.getWorkbook();
        String cellFormula = cell.getCellFormula();
        int sheetIndex = workbook.getSheetIndex(sheet);
        Ptg[] ptgList = new FormulaParsing().getPtg(cellFormula, workbook, FormulaType.forInt(2), sheetIndex);

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

	private boolean checkAllTheSame(List<Cell> cluster) {
		for (int i=0; i<cluster.size()-1; i++) {
			Cell cLeft = cluster.get(i);
			String rcLeft = bu.convertA1ToR1C1(
					cLeft.getRowIndex(), cLeft.getColumnIndex(), cLeft.getCellFormula());
			for (int j=i+1; j<cluster.size(); j++) {
				Cell cRight = cluster.get(j);
				String rcRight = bu.convertA1ToR1C1(
						cRight.getRowIndex(), cRight.getColumnIndex(), cRight.getCellFormula());
				if (! rcLeft.equals(rcRight)) {
					return false;
				}
			}
		}
		
		return true;
	}

	private void reportAll(Instances dataset, List<CellReference> cellRefsInCluster, 
			RealMatrix featureCellM) {
		boolean[] taggedCells = new boolean[cellRefsInCluster.size()];
		Boolean outlierExist = false;
		Boolean inlierExist = false;
		for (int i = 0; i < dataset.size(); i++)
			if (dataset.get(i).value(dataset.numAttributes() - 1) > 0.0001) {
				// True for outlier in taggedCells
				taggedCells[i] = true;
				outlierExist = true;
			} else {
				taggedCells[i] = false;
				inlierExist = true;
			}

		if (outlierExist && inlierExist) {
			detectedSmellyCells.addAll(smellDescriptionDetection(
					taggedCells, featureCellM, cellRefsInCluster));
		}
	}

	private List<Smell> smellDescriptionDetection(boolean[] taggedCells,
			RealMatrix featureCellM, List<CellReference> cellRefsVector) {
		List<Smell> smells = new ArrayList<Smell>();

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

		Map<DecisionTableHashKey, Integer> twoInst = new HashMap<DecisionTableHashKey, Integer>();
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
		ArrayList<Attribute> attributes = new ArrayList<Attribute>();
		List<Instance> instances = new ArrayList<Instance>();

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

		for (Instance inst : instances)
			dataset.add(inst);

		return new Instances(dataset);
	}

	private void detectionFeatureExtraction(Workbook wb, List<Cell> cells) {
		subFtList = new ArrayList<CellFeature>();
		featureVector = new ArrayList<String>();
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
					String ops = "";
					for (String opToken : opTokens) {
						ops = ops + opToken + ",";
					}
					if (!featureVector.contains("Operation=" + ops)) {
						featureVector.add("Operation=" + ops);
					}
				}

				List<String> refTokens = tokens.get(1);
				if (refTokens != null && !refTokens.isEmpty()) {
					ftInList.setRefTokens(refTokens);
					String ref = "";
					for (String refToken : refTokens) {
						ref = ref + refToken + ",";
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
