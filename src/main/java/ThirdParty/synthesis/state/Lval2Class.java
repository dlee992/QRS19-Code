package ThirdParty.synthesis.state;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ThirdParty.synthesis.basic.ProgramLine;
import ThirdParty.synthesis.basic.ProgramLineType;
import ThirdParty.synthesis.basic.Result;
import ThirdParty.synthesis.component.Component;
import ThirdParty.synthesis.component.Components;
import ThirdParty.synthesis.util.Z3Util;

public class Lval2Class {
	public static void tranform(Map<MethodAbstract, Result> rets,
			ClassAbstract ci) {

		for (MethodAbstract ma : ci.methods) {
			Result ret = rets.get(ma);
			Method method = new Method(ret, ma);
			tranformMethod(ma.methodName, method);
		}
	}

	public static void tranformMethod(String mName, Method method) {

		ProgramLine[] lines = method.tranform();

		String progs[] = new String[lines.length];
		for (int i = 0; i < lines.length; i++) {
			ProgramLine line = lines[i];
			ProgramLineType type = line.type;
			if (type == ProgramLineType.STATE_GET) {
				progs[i] = "o" + i + " = " + "state." + line.inputs[0];
			} else if (type == ProgramLineType.INPUT) {
				progs[i] = "o" + i + " = " + "input." + line.inputs[0];
			} else if (type == ProgramLineType.COMPONENT) {
				String right = getComponentProg(line.comp, line.inputs,
						line.cons);
				progs[i] = "o" + i + " = " + right;
			} else if (type == ProgramLineType.STATE_SET) {
				progs[i] = "state." + line.inputs[0] + " = o" + line.inputs[1];
			} else if (type == ProgramLineType.OUTPUT) {
				progs[i] = "ret " + "o" + line.inputs[0];
			}
		}

		System.out.println("Method " + mName + " {");
		for (String str : progs) {
			System.out.println("    " + str);
		}
		System.out.println("}");
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
}
