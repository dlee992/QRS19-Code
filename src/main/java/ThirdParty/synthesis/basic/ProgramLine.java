package ThirdParty.synthesis.basic;

import ThirdParty.synthesis.component.Component;
import ThirdParty.synthesis.component.Components;

public class ProgramLine {

	public ProgramLineType type;
	public Component comp;
	public Integer[] inputs;
	public Object cons;

	public ProgramLine(ProgramLineType type, Component comp, Integer[] inputs) {
		this.type = type;
		this.comp = comp;
		this.inputs = inputs;
	}

	public ProgramLine(ProgramLineType type, Component comp, Integer[] inputs,
			Object cons) {
		this(type, comp, inputs);
		this.cons = cons;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(type);
		sb.append(": ");
		if (comp.getType() == Components.CONSTANT) {
			sb.append(cons);
		} else {
			String[] strs = new String[inputs.length];
			for (int i = 0; i < inputs.length; i++) {
				strs[i] = inputs[i].toString();
			}
			sb.append(comp.getProg(strs));
		}
		return sb.toString();
	}
}
