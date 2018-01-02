package entity;

import clustering.hacClustering.hierarchicalClustering.Distance;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
import utility.BasicUtility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import static programEntry.GP.parameter_3;

public class Cluster {
	
	private String name;
	private List<Cluster> children = null;

	private List<CellReference> clusterCellRefs;
	private List<CellReference> seedReference;
	private List<Cell> clusterCells;
	private List<Cell> seedCells;
	
	private boolean seedOrNot = false;
	
	private double associationValue = 1.0;

	private int upperBorder, belowBorder, leftBorder, rightBorder;

	public double coverage = -1;

	private Distance distance = new Distance();

	@Override
	public String toString () {
		return "Cluster: " + name;
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	@Override
	public boolean equals(Object object) {
		return object != null && this.toString().equals(object.toString());
	}

	public Cluster(String name) {
		this.name = name;
//		System.out.println("cluster name = " + name);
	}


    public Distance getDistance() {
        return distance;
    }

    public Double getWeightValue() {
        return distance.getWeight();
    }

    public void setDistance(Distance distance) {
        this.distance = distance;
    }

	public void addChild(Cluster cluster) {
		getChildren().add(cluster);
	}

	public List<Cluster> getChildren() {
		if (children == null) {
			children = new ArrayList<>();
		}
		return children;
	}

	public String getName() {
		return name;
	}

	public boolean isLeaf() {
		return getChildren().size() == 0;
	}

	public void setParent() {
	}
	
	public List<Cluster> getChildrenCluster(Cluster node) {
		List<Cluster> childrenCluster = new ArrayList<>();
		clusterChildrenSearch(childrenCluster, node);
		return childrenCluster;
	}
	
	private void clusterChildrenSearch(List<Cluster> clusters, Cluster node) {
		if (!node.isLeaf()) {
			for (Cluster clusterChild : node.getChildren()) {
				clusterChildrenSearch(clusters, clusterChild);
			}
		} else {
			clusters.add(node);
		}
	}

	public List<CellReference> extractCellRefs(Cluster cluster, int stage) {
		List<CellReference> cellRefsList = new ArrayList<>();
		for (String name : cluster.getChildrenName(cluster)) {
			CellReference cr = new CellReference(name);
			if (!cellRefsList.contains(cr))
				cellRefsList.add(cr);
		}
		setClusterCellRefs(cellRefsList);

		if (stage == 1) {
			setSeedReference(cellRefsList);
		}
		return cellRefsList;
		
	}

	public List<String> getChildrenName(Cluster node) {
		List<String> childrenName = new ArrayList<>();
		childrenNameSearch(childrenName, node);
		return childrenName;
	}
	
	private void childrenNameSearch(List<String> childrenName, Cluster node) {
		if (!node.isLeaf()) {
			for (Cluster clusterChild : node.getChildren()) {
				childrenNameSearch(childrenName, clusterChild);
			}
		} else {
			childrenName.add(node.getName());
		}

	}

	private void setSeedReference(List<CellReference> cellRefsList) {
		this.seedReference = cellRefsList;
	}

	private void setClusterCellRefs(List<CellReference> cellRefsList) {
		this.clusterCellRefs = cellRefsList;
	}

	public List<Cell> extractCells(Sheet sheet, Cluster cluster, int stage) {
		List<Cell> cellList = new ArrayList<>();
		for (String name : cluster.getChildrenName(cluster)) {
			Cell cell = convertAddToCell(sheet, name);
			cellList.add(cell);
		}
		setClusterCells(cellList);
		if (stage == 1) {
			setSeedCells(cellList);
		}
		return cellList;
	}

	private void setSeedCells(List<Cell> cellList) {
		this.seedCells = cellList;
	}

	private void setClusterCells(List<Cell> cellList) {
		this.clusterCells = cellList;
	}

	private Cell convertAddToCell(Sheet sheet, String address) {
		CellReference cellRef = new CellReference(address);
		Short cellCol = cellRef.getCol();
		int cellRow = cellRef.getRow();
		Row row = sheet.getRow(cellRow);
		return row.getCell(cellCol);
	}

	public List<CellReference> getClusterCellRefs() {
		return this.clusterCellRefs;
	}

	public boolean isSeedOrNot() {
		return seedOrNot;
	}

	public void setSeedOrNot(boolean seedOrNot) {
		this.seedOrNot = seedOrNot;
	}

	public List<Cell> getClusterCells() {
		return clusterCells;
	}

	private double getAssociationValue() {
		return associationValue;
	}

	public void setAssociationValue(double associationValue) {
		this.associationValue = associationValue;
	}

	public List<CellReference> getSeedReference() {
		return seedReference;
	}

	public List<Cell> getSeedCells() {
		return seedCells;
	}

	public Map<CellReference, Double> cellRefsWithValue() {
		Map<CellReference, Double> cellRefsList = new HashMap<CellReference, Double>();
		List<Cluster> clusters = this.getChildrenCluster(this);
		for (Cluster child : clusters) {
			CellReference cr = new CellReference(child.getName());
			if (!cellRefsList.containsKey(cr))
				cellRefsList.put(cr, child.getAssociationValue());
		}

		return cellRefsList;
	}


	public void computeBorders() {
	    upperBorder = belowBorder = seedReference.get(0).getRow();
	    leftBorder  = rightBorder = seedReference.get(0).getCol();

        for (int i = 1; i < seedReference.size(); i++) {
            int row = seedReference.get(i).getRow();
            int col = seedReference.get(i).getCol();
            if (upperBorder > row) upperBorder = row;
            if (belowBorder < row) belowBorder = row;
            if (leftBorder  > col) leftBorder  = col;
            if (rightBorder < col) rightBorder = col;
        }
    }

    public boolean OutsideBorder(CellReference childCR) {
        return upperBorder > childCR.getRow() || belowBorder < childCR.getRow() ||
                leftBorder > childCR.getCol() || rightBorder < childCR.getCol();
    }


    private static Logger logger = LogManager.getLogger(Cluster.class.getName());
    /**
     *
     * @param childCell
     *  the cell ready to be checked.
     * @return
     *  0: throw computation exception;
     *  1: not throw exception, and satisfy weak computation coverage;
     *  2: not throw exception, and not satisfy weak coverage.
     */
    public int checkWeakCoverage(Cell childCell) {
        CellReference childCR = new CellReference(childCell);
        double old_value = 0;
        int return_val = 2;

        Cell logCell = seedCells.get(0);
		logger.debug(logCell.getSheet().getWorkbook() + " - " + logCell.getSheet().getSheetName());

		for (Cell baseCell : seedCells) {
            if (baseCell.getCellTypeEnum() != CellType.FORMULA) continue;
            if (return_val < 2) break;

            try {
				/*
				* compare baseCell and childCR's return value's difference.
				*/
                logger.debug("baseCell = " + baseCell.getAddress()+
                        ", formula = "+ baseCell.getCellFormula());
                FormulaEvaluator evaluator = baseCell.getSheet().getWorkbook().getCreationHelper().
                                                                                    createFormulaEvaluator();
                CellReference baseCR = new CellReference(baseCell);

                old_value = childCell.getNumericCellValue();
                String new_cellFormulaA1 = new BasicUtility().convertA1ToA1(childCR.getRow() - baseCR.getRow(),
                        childCR.getCol() - baseCR.getCol(), baseCell.getCellFormula());
                logger.debug("dataCell's oldValue=" + old_value);

                if (new_cellFormulaA1 == null || new_cellFormulaA1.contains("!")) {
                    return 0;
                }
                else {
                    childCell.setCellType(CellType.FORMULA);
                    childCell.setCellFormula(new_cellFormulaA1);
//                    childCell.setCellFormula("2/0");

                    logger.debug("dataCell = " + childCell.getAddress()+
                            ", formula = "+childCell.getCellFormula());

                    CellType result_type = evaluator.evaluateFormulaCellEnum(childCell);
                    logger.debug("result type: "+result_type);

                    switch (result_type) {
                        case NUMERIC:
                            double new_value = childCell.getNumericCellValue();

                            logger.debug("dataCell's newValue="+new_value);

//                            if (Math.abs(new_value / old_value - 1) <= parameter_3) {
//                                logger.debug("The ratio is leq to " + parameter_3);
//                                return_val = 1;
//                            }
//                            else
                                return_val = 2;
                            break;

                        case _NONE://impossible

						case ERROR:

                        case STRING:

                        case FORMULA:

                        case BLANK:

                        case BOOLEAN:

                        default:
                            return_val = 0;
                            break;
                    }
                }
            } catch (Exception e) {
                logger.debug("throw an external exception: " + e.toString());
                childCell.setCellValue(old_value);
                childCell.setCellType(CellType.NUMERIC);
                return_val = 0;

            } finally {
                childCell.setCellValue(old_value);
                childCell.setCellType(CellType.NUMERIC);
            }
        }

        logger.debug("return val condition = " + return_val);
	    return return_val;
    }

	public void removeChild(Cell cell) {
		CellReference cr = new CellReference(cell);
		Cluster cluster = new Cluster(cr.formatAsString());

        children.remove(cluster);
        clusterCells.remove(cell);
        clusterCellRefs.remove(cr);
	}
}
