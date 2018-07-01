package thirdparty.CACheck.formula;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.apache.poi.ss.usermodel.Cell;

import thirdparty.CACheck.CellArray;
import thirdparty.CACheck.R1C1Cell;
import thirdparty.CACheck.cellarray.inference.FormulaPattern;
import thirdparty.CACheck.util.Log;
import thirdparty.CACheck.util.Utils;

public class ParseFormula {

	public static FormulaPattern parsePattern(CellArray ca, Cell cell) {

		List<Object> pattern = parse(ca, cell.getRowIndex(),
				cell.getColumnIndex(), cell.getCellFormula());

		FormulaPattern fp = new FormulaPattern(pattern);
		return fp;
	}

	public static List<Object> parse(CellArray ca, Cell cell) {
		return parse(ca, cell.getRowIndex(), cell.getColumnIndex(),
				cell.getCellFormula());
	}

	public static List<Object> parse(CellArray ca, int curRow, int curColumn,
			String formula) {

		List<Object> spec = null;
		try {
			spec = parseFormula(curRow, curColumn, formula);
		} catch (RuntimeException e) {
			Log.logNewLine("Unsupported formula: " + formula, Log.writer);
			throw e;
		}

		Set<R1C1Cell> inputs = FormulaPattern.getInputs(spec);
		for (R1C1Cell input : inputs) {
			boolean isConstant = false;
			for (R1C1Cell tmp : ca.constants) {
				if (tmp.getA1Cell().equals(input.getA1Cell())) {
					isConstant = true;
					break;
				}
			}
			if (isConstant) {
				if (ca.isRowCA && input.columnRelative || !ca.isRowCA
						&& input.rowRelative) {
					input.referChanged = true;
				}
				input.rowRelative = false;
				input.columnRelative = false;
			}

			if (input.rowRelative && !input.columnRelative
					|| !input.rowRelative && input.columnRelative) {
				if (ca.isRowCA) {
					input.columnRelative = true;
				} else {
					input.rowRelative = true;
				}
			}
		}

		if (inputs.size() == 0) {
			return null;
		}

		return spec;
	}

	public static List<Object> parseFormula(int curRow, int curColumn,
			String formula) {
		while (formula.startsWith("+") || formula.startsWith(" ")) {
			// remove the first + and space
			formula = formula.substring(1);
		}
		if (formula.contains("!")) {
			// ignore the external worksheet for now.
			return new ArrayList<Object>();
		}

		Stack<Object> stack = new Stack<Object>();

		List<Object> res = new ArrayList<Object>();

		for (int i = 0; i < formula.length();) {
			int start = i;
			int end = i;

			if (formula.charAt(i) == '(') {
				stack.push("(");
				i++;
			} else if (formula.charAt(i) == ')') {
				while (!stack.empty()) {
					Object op = stack.pop();
					if (op == "(")
						break;
					else
						res.add(op);
				}
				i++;
			} else if (!Utils.isLetter(formula.charAt(i))
					&& formula.charAt(i) != '$') {
				// it's a number
				if (Utils.isNumber(formula.charAt(i))) {
					for (; i < formula.length()
							&& (Utils.isNumber(formula.charAt(i)) || formula
									.charAt(i) == '.'); i++)
						;
					end = i;
					Double num = Double.parseDouble(formula.substring(start,
							end));

					// it is %
					if (i < formula.length() && formula.charAt(i) == '%') {
						num = num / 100;
						i++;
					}

					res.add(num);

					continue;
				}

				// it's the operation of excels
				for (; i < formula.length()
						&& (!Utils.isLetter(formula.charAt(i))
								&& !Utils.isNumber(formula.charAt(i))
								&& formula.charAt(i) != '$'
								&& formula.charAt(i) != '(' && formula
								.charAt(i) != ')'); i++)
					;
				end = i;

				String op = formula.substring(start, end).trim();

				if (!stack.empty()) {
					while (!stack.empty()) {
						Object op1 = stack.pop();
						if (Operation.priority(op) > Operation
								.priority((String) op1)) {
							stack.push(op1);
							stack.push(op);
							break;
						} else {
							res.add(op1);
							if (stack.empty()) {
								stack.push(op);
								break;
							}
						}
					}
				} else {
					stack.push(op);
				}

				continue;
			} else {
				if (Utils.isLetter(formula.charAt(i))
						|| formula.charAt(i) == '$') {
					// deal with the "row"
					if (formula.charAt(i) == '$') {
						i++; // skip the $
					}
					for (; i < formula.length()
							&& Utils.isLetter(formula.charAt(i)); i++)
						;

					if (Utils.isNumber(formula.charAt(i))
							|| formula.charAt(i) == '$') {
						// deal with the "column"
						if (formula.charAt(i) == '$') {
							i++; // skip the $
						}
						for (; i < formula.length()
								&& Utils.isNumber(formula.charAt(i)); i++)
							;
						end = i;
						// the input cell with absolute column
						res.add(Utils.extractCell(curRow, curColumn,
								formula.substring(start, end)));
					} else {
						// it's the functions of the excel
						end = i;

						int parentheses = 0;
						int funEnd = start;
						for (int j = end; j < formula.length(); j++) {
							if (formula.charAt(j) == '(') {
								parentheses++;
							}
							if (formula.charAt(j) == ')') {
								parentheses--;
							}
							if (parentheses == 0) {
								funEnd = j;
								break;
							}
						}
						Function fun = parseFunction(curRow, curColumn,
								formula.substring(start, end),
								formula.substring(end + 1, funEnd));
						res.add(fun);

						i = funEnd + 1;
					}
				}
			}
		}

		while (!stack.empty()) {
			res.add(stack.pop());
		}

		return res;
	}

	private static Function parseFunction(int row, int column, String funName,
			String paraString) {
		Function fun = Function.createFunction(funName);

		// SUM(A1,B1*C1)
		String[] paras = paraString.split(",");
		for (String para : paras) {
			if (para.contains(":")) {
				// SUM(A1:A3)
				List<R1C1Cell> ps = Utils.extractParameters(row, column, para);
				for (R1C1Cell p : ps) {
					List<Object> tmp = new ArrayList<Object>();
					tmp.add(p);
					fun.inputs.add(tmp);
				}
			} else {
				// SUM(A1*B3)
				List<Object> p = parseFormula(row, column, para);
				fun.inputs.add(p);
			}
		}

		return fun;
	}

	// depciated.
	public static void print(List<Object> formula) {
		for (Object o : formula) {
			System.out.print(o + "   ");
		}
		System.out.println();
	}

	public static String getA1Pattern(int curRow, int curCol, List<Object> spec) {
		Stack<String> stack = new Stack<String>();
		for (Object o : spec) {
			if (o instanceof R1C1Cell) {
				R1C1Cell cell = (R1C1Cell) o;
				String c = cell.getA1Cell(curRow, curCol);
				stack.push(c);
			} else if (o instanceof Double) {
				stack.push(o.toString());
			} else if (o instanceof Function) {
				Function fun = (Function) o;
				String ret = fun.getA1Formula(curRow, curCol);
				stack.push(ret);
			} else {
				String op2 = stack.pop();
				String ret = null;
				if (stack.isEmpty()) {
					ret = "(" + o + op2 + ")";
				} else {
					String op1 = stack.pop();
					ret = "(" + op1 + o + op2 + ")";
				}

				stack.push(ret);
			}
		}

		String ret = stack.pop();
		return ret;
	}

	public static String getR1C1Pattern(List<Object> pattern) {
		Stack<String> stack = new Stack<String>();
		for (Object o : pattern) {
			if (o instanceof R1C1Cell) {
				R1C1Cell cell = (R1C1Cell) o;
				stack.push(cell.toString());
			} else if (o instanceof Double) {
				stack.push(o.toString());
			} else if (o instanceof Function) {
				Function fun = (Function) o;
				String ret = fun.getR1C1Formula();
				stack.push(ret);
			} else {
				String op2 = stack.pop();
				String ret = null;
				if (stack.isEmpty()) {
					ret = "(" + o + op2 + ")";
				} else {
					String op1 = stack.pop();
					ret = "(" + op1 + o + op2 + ")";
				}

				stack.push(ret);
			}
		}

		String ret = stack.pop();
		return ret;
	}
}
