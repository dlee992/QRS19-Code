package featureExtraction.strongFeatureExtraction;

import utility.BasicUtility;
import entity.Cluster;
import entity.R1C1Cell;
import org.apache.poi.hssf.usermodel.HSSFEvaluationWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.formula.*;
import org.apache.poi.ss.formula.ptg.*;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFEvaluationWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.List;
import java.util.Stack;

public class AST {
    private Sheet  sheet;
	private String formula;
	private String address;
	private BasicUtility bu = new BasicUtility();
	
	public AST(String formula, String address, Sheet sheet){
		this.sheet   = sheet;
		this.address = address;
		this.formula = formula;
	}
		
	public Cluster createTree(){
		
		Cluster result = null;
	    Stack<Cluster> stack = new Stack<Cluster>();

        Workbook wb = sheet.getWorkbook();
		FormulaParsingWorkbook fpWorkbook = null;
        FormulaRenderingWorkbook frWorkbook = null;
		if (wb.getSpreadsheetVersion().name().equals("EXCEL97")) {
            fpWorkbook = HSSFEvaluationWorkbook.create((HSSFWorkbook) wb);
            frWorkbook = HSSFEvaluationWorkbook.create((HSSFWorkbook) wb);
		}
		else if (wb.getSpreadsheetVersion().name().equals("EXCEL2007")) {
            fpWorkbook = XSSFEvaluationWorkbook.create((XSSFWorkbook) wb);
            frWorkbook = XSSFEvaluationWorkbook.create((XSSFWorkbook) wb);
		}

		try{
			Ptg[] ptgs = FormulaParser.parse(formula, fpWorkbook, FormulaType.forInt(2), wb.getSheetIndex(sheet));
			
			R1C1Cell preR1C1Cell;
			R1C1Cell curr1c1Cell;
			
			CellReference cr = new CellReference(address);
			
			int col = (int) cr.getCol();
			int row = cr.getRow();
			
	        if (ptgs == null || ptgs.length == 0) {
	            throw new IllegalArgumentException("ptgs must not be null");
	        }
	        

	        for (Ptg ptg : ptgs) {
	        	
	            // what about MemNoMemPtg?
	            if(ptg instanceof MemAreaPtg || ptg instanceof MemFuncPtg || ptg instanceof MemErrPtg) {
	                // marks the start of a list of area expressions which will be naturally combined
	                // by their trailing operators (e.g. UnionPtg)
	                // put comment and throw exception in toFormulaString() of these classes
	                continue;
	            }
	            if (ptg instanceof ParenthesisPtg) {
	            	/*Cluster cluster =stack.pop();
	            	cluster.addChild(new Cluster ("("));
	            	cluster.addChild(new Cluster(")"));
	                stack.push (cluster);*/
	                continue;
	            }
	           
	            if (ptg instanceof AttrPtg) {
	                AttrPtg attrPtg = ((AttrPtg) ptg);
	                if (attrPtg.isOptimizedIf() || attrPtg.isOptimizedChoose() || attrPtg.isSkip()) {
	                    continue;
	                }
	                if (attrPtg.isSpace()) {
	                    // POI currently doesn't render spaces in formulas
	                    continue;
	                    // but if it ever did, care must be taken:
	                    // tAttrSpace comes *before* the operand it applies to, which may be consistent
	                    // with how the formula text appears but is against the RPN ordering assumed here
	                }
	                if (attrPtg.isSemiVolatile()) {
	                    // similar to tAttrSpace - RPN is violated
	                    continue;
	                }
	                if (attrPtg.isSum()) {
	                    
	                    Cluster node = new Cluster(attrPtg.toFormulaString());
	                    node = getOperands(node, stack, attrPtg.getNumberOfOperands());
	                    stack.push(node);
	                    continue;
	                }
	                throw new RuntimeException("Unexpected tAttr: " + attrPtg.toString());
	            }

	            if (ptg instanceof WorkbookDependentFormula) {
	                WorkbookDependentFormula optg = (WorkbookDependentFormula) ptg;
	                Cluster node = new Cluster (optg.toFormulaString(frWorkbook));
	                stack.push(node);
	                continue;
	            }
	            if (! (ptg instanceof OperationPtg)) {
	            	String content;
	            	if (ptg instanceof AreaPtg){
		            	String[] cell = ptg.toFormulaString().split(":");
		    			preR1C1Cell = bu.extractCell(row, col, cell[0]);
		    			curr1c1Cell = bu.extractCell(row, col, cell[1]);
//		    			List<R1C1Cell> individualCells= bu.getAreaCells(preR1C1Cell, curr1c1Cell);
//		    			String cl = "";
//		    			for (R1C1Cell one: individualCells){
//		    				cl = "{" + one.toString() + "}" + cl;
//		    			}
		    			//TODO: do not unfold the cell range
                        String cl = preR1C1Cell.toString() + ":" + curr1c1Cell.toString();
		    			stack.push(new Cluster(cl));


	            	}
	           
		            else if (ptg instanceof RefPtg){
		            	content = bu.extractCell(row, col, ptg.toFormulaString()).toString();
		            	Cluster node = new Cluster (content);
			            stack.push(node);
		            }
		            else if (ptg instanceof ScalarConstantPtg){
		            	Cluster node = new Cluster ("Scalar");
		                stack.push(node);
		            }
		            else{
		            	content = ptg.toFormulaString();
		            	Cluster node = new Cluster (content);
			            stack.push(node);
		            }
	            	
	            	continue;
	            }
	       
	            
	            OperationPtg o = (OperationPtg) ptg;
	            Cluster node;
	            if (o instanceof ValueOperatorPtg){
	            	String str = o.toString();
	            	node = new Cluster (str.substring(str.lastIndexOf(".")+1, str.length()-3));
	            }
	            else {
	                node = new Cluster (o.toFormulaString());
	            }
	            node = getOperands (node, stack, o.getNumberOfOperands());
	            stack.push(node);
	        }
	        
	        if(stack.isEmpty()) {
	            // inspection of the code above reveals that every stack.pop() is followed by a
	            // stack.push(). So this is either an internal error or impossible.
	            throw new IllegalStateException("Stack underflow");
	        }
	        result = stack.pop();
	        if(!stack.isEmpty()) {
	            // Might be caused by some tokens like AttrPtg and Mem*Ptg, which really shouldn't
	            // put anything on the stack
	            throw new IllegalStateException("too much stuff left on the stack");
	        }
			}catch(Exception e){
				e.printStackTrace();
			}
		
	        return result;
	    }

	    private Cluster getOperands(Cluster node, Stack<Cluster> stack, int nOperands) {
	         
	        for (int j = nOperands-1; j >= 0; j--) { // reverse iteration because args were pushed in-order
	            if(stack.isEmpty()) {
	               String msg = "Too few arguments supplied to operation. Expected (" + nOperands
	                    + ") operands but got (" + (nOperands - j - 1) + ")";
	                throw new IllegalStateException(msg);
	            }
	            node.addChild(stack.pop());
	        }
	        return node;
	    }
}
