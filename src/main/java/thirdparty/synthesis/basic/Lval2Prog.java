package thirdparty.synthesis.basic;

import java.util.ArrayList;
import java.util.List;

import thirdparty.synthesis.component.Component;
import thirdparty.synthesis.component.Components;
import thirdparty.synthesis.util.Z3Util;

public class Lval2Prog {
	public static void tranform(Result res, ProgramAbstract pa,
			boolean isFormula) {
		Program prog = new Program(res, pa);
		ProgramLine[] lines = prog.tranform();

		if (isFormula) {
			ProgramLine resLine = lines[lines.length - 1];
			Integer ret = resLine.inputs[0];
			String inputs[] = new String[pa.inputTypes.size()];
			for (int i = 0; i < pa.inputTypes.size(); i++) {
				inputs[i] = "x" + (i + 1);
			}
			String formula = constructPara(ret, lines, inputs);

			StringBuffer result = new StringBuffer();
			result.append("f(");
			for (int i = 0; i < inputs.length; i++) {
				result.append(inputs[i] + ",");
			}
			result.delete(result.length() - 1, result.length());
			result.append(") = ");
			result.append(formula);
			System.out.println(result);
		} else {
			String progs[] = new String[lines.length];
			for (int i = 0; i < lines.length; i++) {
				ProgramLine line = lines[i];
				ProgramLineType type = line.type;
				if (type == ProgramLineType.INPUT) {
					progs[i] = "o" + i + " = " + "i" + line.inputs[0];
				} else if (type == ProgramLineType.OUTPUT) {
					progs[i] = "ret " + "o" + line.inputs[0];
				} else {
					String right = getComponentProg(line.comp, line.inputs,
							line.cons);
					progs[i] = "o" + i + " = " + right;
				}
			}

			for (String str : progs) {
				System.out.println(str);
			}
		}
	}

	private static String getComponentProg(Component comp, Integer[] inputs,
			Object cons) {
		List<String> paras = new ArrayList<String>();

		if (comp.getType() == Components.CONSTANT) {
			paras.add("" + Z3Util.printObject(cons));
		} else {
			for (Integer input : inputs) {
				String para = "o" + input;
				paras.add(para);
			}
		}

		return comp.getProg(paras.toArray(new String[0]));
	}

	private static String constructPara(int para, ProgramLine[] lines,
			String[] inputs) {

		ProgramLine paraLine = lines[para];

		if (paraLine.type == ProgramLineType.INPUT) {
			int index = paraLine.inputs[0];
			return inputs[index];
		} else if (paraLine.type == ProgramLineType.COMPONENT) {
			if (paraLine.comp.getType() == Components.CONSTANT) {
				return "" + paraLine.cons;
			}
			String[] paras = new String[paraLine.inputs.length];
			for (int i = 0; i < paraLine.inputs.length; i++) {
				paras[i] = constructPara(paraLine.inputs[i], lines, inputs);
			}
			return "(" + paraLine.comp.getProg(paras) + ")";
		}

		return null;
	}
}
