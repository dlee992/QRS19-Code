package clustering.hacClustering;

import clustering.hacClustering.hierarchicalClustering.ClusterPair;
import convenience.RTED;
import entity.Cluster;
import entity.R1C1Cell;
import featureExtraction.strongFeatureExtraction.AST;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
import util.LblTree;
import utility.BasicUtility;
import utility.FormulaParsing;

import java.util.*;

import static java.lang.System.out;
import static programEntry.GP.*;

public class HacClustering {
	private static Logger logger = LogManager.getLogger(HacClustering.class.getName());

	private Map<String, List<String>> formulaInfoList = null;
	private double[][] distances;
	private List<String> formulaCellAdd;
	private BasicUtility bu = new BasicUtility();

    public HacClustering(Map<String, List<String>> formulaInfoList) {
		this.formulaInfoList = formulaInfoList;
	}

	public void computeDistance(TreeEditDistance ted) {
		distances = new double[formulaInfoList.size()]
				[formulaInfoList.size()];
		int m =0;
		formulaCellAdd = new ArrayList<>();

//		System.out.println("formulaInfoList size = " + formulaInfoList.size());
		int index = 0;
		for (Map.Entry<String, List<String>> itemOut : formulaInfoList.entrySet())
		{
			formulaCellAdd.add(itemOut.getKey());

			String[] left = new String [4];
			left[0]= itemOut.getKey();
			left[1] = itemOut.getValue().get(0);
			left[2] = itemOut.getValue().get(1);

			AST astLeft = new AST(left[1], left[0], ted.sheet);
            String treeStr = "";
            Cluster clLeft = astLeft.createTree();
            if (clLeft != null) {
				treeStr = ted.childrenSearch(clLeft);
            }
//            out.println(left[0] + "'s AST Tree = " + treeStr);
            String asTreeStrLeft = treeStr.replace(":", " to ");
            LblTree asTreeLeft = LblTree.fromString(asTreeStrLeft);

			int n = 0;
			for (Map.Entry<String, List<String>> itemIn : formulaInfoList.entrySet())
			{
			    if (m >= n) {
			        n++;
			        continue;
                }

                System.out.println("tree distance comparision's index = " + ++index);

				String[] right = new String [4];
				right[0] = itemIn.getKey();
				right[1] = itemIn.getValue().get(0);
				right[2] = itemIn.getValue().get(1);

                AST astRight = new AST(right[1], right[0], ted.sheet);
                Cluster clRight = astRight.createTree();

                if (clRight != null)
                    treeStr = ted.childrenSearch(clRight);

                String asTreeStrRight = treeStr.replace(":", " to ");
                LblTree asTreeRight = LblTree.fromString(asTreeStrRight);

                int leftNodeNum2 = asTreeLeft.getNodeCount();
                int rightNodeNum2 = asTreeRight.getNodeCount();
                double nodeSum2 = leftNodeNum2 + rightNodeNum2;
                double astDist = (RTED.computeDistance(asTreeStrLeft,asTreeStrRight)) / nodeSum2;

//                out.printf("%s = %s, %s = %s\n", left[0], asTreeStrLeft, right[0], asTreeStrRight);

                //compute CDT.
                String dpTreeStrLeft = BasicUtility.cellDependencies(ted.sheet, left[0], 0);
                LblTree dpTreeLeft = LblTree.fromString(dpTreeStrLeft);
                int leftNodeNum = dpTreeLeft.getNodeCount();

                String dpTreeStrRight = BasicUtility.cellDependencies(ted.sheet, right[0], 0);
                LblTree dpTreeRight = LblTree.fromString(dpTreeStrRight);
                int rightNodeNum = dpTreeRight.getNodeCount();

                double nodeSum = leftNodeNum + rightNodeNum;
                double dpDist = (RTED.computeDistance(dpTreeStrLeft, dpTreeStrRight)) / nodeSum;

//                out.printf("%s = %s, %s = %s\n", left[0], dpTreeStrLeft, right[0], dpTreeStrRight);

				//TODO: 似乎这些计算是不可避免的
				distances[n][m] = distances[m][n] = astDist + dpDist - astDist*dpDist;

				n++;
			}
			m++;
		}
	}

	public List<Cluster> clustering(TreeEditDistance ted) {
		computeDistance(ted);
		return performClustering();
	}

	public List<Cluster> clusteringWrapper(List<Cluster> caCheckCluster, Set<String> caCheckFormula) {

		//TODO: cluster initialization
		List<Cluster> clusters = new ArrayList<>();

		for (Cluster cluster: caCheckCluster) {
			clusters.add(cluster);
		}

		for (String aFormulaCellAdd : formulaCellAdd)
		{
			if (!caCheckFormula.contains(aFormulaCellAdd)) {
				clusters.add(new Cluster(aFormulaCellAdd));
			}
		}
		System.out.println();

		//TODO: clustering process
		clusteringCoreProcess(clusters);

		return clusters;
	}

	private List<Cluster> performClustering() {
		//TODO: cluster initialization
		List<Cluster> clusters = new ArrayList<Cluster>();
		for (String aFormulaCellAdd : formulaCellAdd)
			clusters.add(new Cluster(aFormulaCellAdd));

		//TODO: clustering process
		clusteringCoreProcess(clusters);
		
		return clusters;
	}

	public void clusteringCoreProcess(List<Cluster> clusters)
	{
	    /*
	    TODO:
	    显然可以加一个优化：预先把完全一样的格放在同一个类中
	     */




		double minDist = 0;
		int clusterIndex = 0;
		double eps = 0.02;
		while (minDist <= eps) {
			minDist = 0.5;
			Cluster clusterLeft = null;
			Cluster clusterRight = null;

			for (int i=0;i<=clusters.size()-2;i++) {
				for (int j=i+1;j<=clusters.size()-1;j++) {
					double tmpDist = computeDist(clusters.get(i), clusters.get(j));
					if (tmpDist < minDist) {
						minDist = tmpDist;
						clusterLeft = clusters.get(i);
						clusterRight = clusters.get(j);
					}
				}
			}

			if (minDist <= eps) {
				//System.out.println("Here to merge");

				Cluster pCluster = new Cluster("#"+(++clusterIndex));
				//System.out.printf("Cluster name: %s && %s = %s\n", clusterLeft.getName(), clusterRight.getName(),
				//		pCluster.getName());
				pCluster.addChild(clusterLeft);
				pCluster.addChild(clusterRight);
				assert clusterLeft != null;
				clusterLeft.setParent();
				clusterRight.setParent();

				clusters.add(pCluster);
				clusters.remove(clusterLeft);
				clusters.remove(clusterRight);

			}
		}
	}

	private double computeDist(Cluster clusterLeft, Cluster clusterRight) {
		double sum = 0;
		List<Cluster> leafLefts = clusterLeft.getChildrenCluster(clusterLeft);
		List<Cluster> leafRights = clusterRight.getChildrenCluster(clusterRight);

//		System.out.println("formulaCellAdd size = " + formulaCellAdd.size());

		for (Cluster leafLeft : leafLefts) {
			for (Cluster leafRight : leafRights) {
				int leafIndexLeft = formulaCellAdd.indexOf(leafLeft.getName());
				int leafIndexRight = formulaCellAdd.indexOf(leafRight.getName());

				try {
					sum += distances[leafIndexLeft][leafIndexRight];
				}
				catch (ArrayIndexOutOfBoundsException e) {
					if (leafIndexLeft == -1) {
						System.out.printf("leafLeft = %s\n", leafLeft.getName());
					}
					if (leafIndexRight == -1) {
						System.out.printf("leafRight = %s\n", leafRight.getName());
					}

					//FIXME: refer to BasicUtility.java line 374.
					return formulaCellAdd.size() * formulaCellAdd.size();
				}
			}

		}
		sum = sum/(leafLefts.size()*leafRights.size());
		
		return sum;
	}

	/*
	public List<Cluster> extend_seed_cluster_one(Sheet sheet, List<Cluster> clusters) {
		List<Cluster> stageIClusters = new ArrayList<Cluster>();
		for (Cluster cluster : clusters)
			if (cluster.getChildren().size() == 0) {
				R1C1Cell r1C1Cell = bu.extractCell(0, 0, cluster.getName());
				Cluster root_cluster = extend_seed_cluster(sheet, r1C1Cell, cluster, clusters, stageIClusters);
				stageIClusters.add(root_cluster);
			}
			else if (O2_2) {
				List<CellReference> crLists = cluster.extractCellRefs(cluster, 1);
				Cluster root_cluster = cluster;
				for (CellReference cr : crLists) {
					R1C1Cell r1C1Cell = bu.extractCell(0, 0, cr.formatAsString());
					root_cluster = extend_seed_cluster(sheet, r1C1Cell, root_cluster, clusters, stageIClusters);
				}
				stageIClusters.add(root_cluster);
			}
			else {
				stageIClusters.add(cluster);
			}
		return stageIClusters;
	}
*/
	private Cluster extend_seed_cluster(Sheet sheet, R1C1Cell r1C1Cell, Cluster root_cluster, List<Cluster> clusters, List<Cluster> stageIClusters) {
		List<R1C1Cell> referenced_cells = new FormulaParsing().getOperator(sheet, r1C1Cell.column, r1C1Cell.row);
		Cluster new_cluster;

		if (isOrNotSameDirection("Row", r1C1Cell, referenced_cells)) {
			int offset=-1;
			while (r1C1Cell.row + offset >= 0) {
				Row upper_row = sheet.getRow(r1C1Cell.row + offset);
				if (upper_row != null) {
					Cell cur_cell = upper_row.getCell(r1C1Cell.column);
					if (cur_cell != null) {
						new_cluster = isOrNotAdd(sheet, r1C1Cell, cur_cell, root_cluster, clusters, stageIClusters);
						if (new_cluster == null) break;
						root_cluster = new_cluster;
					}
				}
				offset--;
			}

                offset = 1;
                while (r1C1Cell.row + offset <= sheet.getLastRowNum()) {
                    Row below_row = sheet.getRow(r1C1Cell.row + offset);
                    if (below_row != null) {
                        Cell cur_cell = below_row.getCell(r1C1Cell.column);
                        if (cur_cell != null) {
                            new_cluster = isOrNotAdd(sheet, r1C1Cell, cur_cell, root_cluster, clusters, stageIClusters);
                            if (new_cluster == null) break;
                            root_cluster = new_cluster;
                        }
                    }
                    offset++;
                }

            } else if (isOrNotSameDirection("Col", r1C1Cell, referenced_cells)) {
                int offset = -1;
                while (r1C1Cell.column + offset >= 0) {
                    Row row = sheet.getRow(r1C1Cell.row);
                    if (row != null) {
                        Cell cur_cell = row.getCell(r1C1Cell.column + offset);
                        if (cur_cell != null) {
                            new_cluster = isOrNotAdd(sheet, r1C1Cell, cur_cell, root_cluster, clusters, stageIClusters);
                            if (new_cluster == null) break;
                            root_cluster = new_cluster;
                        }
                    }
                    offset --;
                }

                offset = +1;
                while (r1C1Cell.column + offset <= 255) {
                    Row row = sheet.getRow(r1C1Cell.row);
                    if (row != null) {
                        Cell cur_cell = row.getCell(r1C1Cell.column + offset);
                        if (cur_cell != null) {
                            new_cluster = isOrNotAdd(sheet, r1C1Cell, cur_cell, root_cluster, clusters, stageIClusters);
                            if (new_cluster == null) break;
                            root_cluster = new_cluster;
                        }
                    }
                    offset ++;
                }
            }

        return root_cluster;
	}

	private Cluster isOrNotAdd(Sheet sheet, R1C1Cell r1C1Cell, Cell cur_cell, Cluster root_cluster,
							   List<Cluster> clusters, List<Cluster> stageIClusters){
        if (cur_cell.getCellTypeEnum() == CellType.NUMERIC && cur_cell.getNumericCellValue() != (double) 0) {
            Cell baseCell = sheet.getRow(r1C1Cell.row).getCell(r1C1Cell.column);
            FormulaEvaluator evaluator = sheet.getWorkbook().getCreationHelper().createFormulaEvaluator();
            Cluster new_cluster = null;

            boolean isContain = false;
            CellReference cr = new CellReference(cur_cell);

            for (Cluster cluster: stageIClusters) {
                cluster.extractCellRefs(cluster, 1);
                if (cluster.getClusterCellRefs().contains(cr)) {
                    isContain = true;
                    break;
                }
            }

            for (Cluster cluster: clusters) {
                cluster.extractCellRefs(cluster, 1);
                if (cluster.getClusterCellRefs().contains(cr)) {
                    isContain = true;
                    break;
                }
            }
            if (isContain) return null;

			//logger.debug("baseCell: row=%d, column=%d, formula=%s", baseCell.getRowIndex(),
			//		baseCell.getColumnIndex(), baseCell.getCellFormula());
            double old_value = cur_cell.getNumericCellValue();
            String new_cellFormulaA1 = bu.convertA1ToA1(cr.getRow() - r1C1Cell.row, cr.getCol() - r1C1Cell.column,
					baseCell.getCellFormula());

            if (new_cellFormulaA1 != null) {
                cur_cell.setCellType(CellType.FORMULA);
                cur_cell.setCellFormula(new_cellFormulaA1);
				//logger.debug("curCell: row=%d, column=%d, formula=%s", cur_cell.getRowIndex(),
				//		cur_cell.getColumnIndex(), cur_cell.getCellFormula());
                int result_type = evaluator.evaluateFormulaCell(cur_cell);
                if (result_type != Cell.CELL_TYPE_NUMERIC) {
                    cur_cell.setCellValue(old_value);
                    cur_cell.setCellType(CellType.NUMERIC);
                    return null;
                }
                double new_value = cur_cell.getNumericCellValue();
                cur_cell.setCellValue(old_value);
                cur_cell.setCellType(CellType.NUMERIC);

                /*
                strong coverage condition.
                 */
//                if (logicalOperator) {
//					if (Math.abs(old_value - new_value) <= parameter_1 &&
//							Math.abs((old_value - new_value) / old_value) <= parameter_2
//							) {
//						if (dataCellSet.contains(cr) == false) { // avoid a data cell is added into two clusters.
//							ClusterPair cp = new ClusterPair(root_cluster, new Cluster(cr.formatAsString()), null);
//							new_cluster = cp.agglomerate(null);
//						}
//					}
//					else return null;
//				}
//				else {
//					if (Math.abs(old_value - new_value) <= parameter_1 ||
//							Math.abs((old_value - new_value) / old_value) <= parameter_2
//							) {
//						if (dataCellSet.contains(cr) == false) { // avoid a data cell is added into two clusters.
//							ClusterPair cp = new ClusterPair(root_cluster, new Cluster(cr.formatAsString()), null);
//							new_cluster = cp.agglomerate(null);
//						}
//					}
//					else return null;
//				}

                return new_cluster;
            }
            else return null;

        }

        return null;
	}

	private boolean isOrNotSameDirection(String dir, R1C1Cell r1C1Cell, List<R1C1Cell> referenced_cells) {
		if (dir.equals("Row")) {
			int row_base = r1C1Cell.row;
			for (R1C1Cell cell : referenced_cells)
				if (row_base != cell.row)
					return false;
			return true;
		}
		else {
			int col_base = r1C1Cell.column;
			for (R1C1Cell cell : referenced_cells)
				if (col_base != cell.column)
					return false;
			return true;
		}
	}



}
