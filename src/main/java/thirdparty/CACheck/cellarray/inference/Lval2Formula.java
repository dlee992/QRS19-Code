package thirdparty.CACheck.cellarray.inference;

import java.util.Set;

import thirdparty.CACheck.CellArray;
import thirdparty.CACheck.R1C1Cell;
import thirdparty.synthesis.basic.Program;
import thirdparty.synthesis.basic.ProgramLine;
import thirdparty.synthesis.basic.ProgramLineType;
import thirdparty.synthesis.basic.ProgramAbstract;
import thirdparty.synthesis.basic.Result;
import thirdparty.synthesis.component.Components;

public class Lval2Formula {
	public static String tranform(CellArray ca, Result res,
			ProgramAbstract program, Set<R1C1Cell> inputs, boolean A1Format) {
		Program prog = new Program(res, program);
		ProgramLine[] lines = prog.tranform();

		ProgramLine resLine = lines[lines.length - 1];

		Integer ret = resLine.inputs[0];

		String formula = constructPara(ca, ret, lines, inputs, A1Format);

		return formula;
	}

	private static String constructPara(CellArray ca, int para,
			ProgramLine[] lines, Set<R1C1Cell> inputs, boolean A1Format) {

		ProgramLine paraLine = lines[para];

		if (paraLine.type == ProgramLineType.INPUT) {
			int index = paraLine.inputs[0];
			R1C1Cell[] is = inputs.toArray(new R1C1Cell[0]);
			if (A1Format) {
				int row = 0;
				int col = 0;
				if (ca.isRowCA) {
					row = ca.rowOrColumn;
					col = ca.start;
				} else {
					row = ca.start;
					col = ca.rowOrColumn;
				}
				return is[index].getA1Cell(row, col);
			} else {
				return is[index].toString();
			}
		} else if (paraLine.type == ProgramLineType.COMPONENT) {
			if (paraLine.comp.getType() == Components.CONSTANT) {
				return "" + paraLine.cons;
			}
			String[] paras = new String[paraLine.inputs.length];
			for (int i = 0; i < paraLine.inputs.length; i++) {
				paras[i] = constructPara(ca, paraLine.inputs[i], lines, inputs,
						A1Format);
			}
			return "(" + paraLine.comp.getProg(paras) + ")";
		}

		return null;
	}
}
