package ThirdParty.synthesis.state;

import java.util.List;

import ThirdParty.synthesis.basic.IOType;
import ThirdParty.synthesis.basic.ProgramLine;
import ThirdParty.synthesis.basic.ProgramLineType;
import ThirdParty.synthesis.basic.Result;
import ThirdParty.synthesis.basic.Type;
import ThirdParty.synthesis.component.Component;
import ThirdParty.synthesis.component.Components;

public class Method {

	private Result res;
	private MethodAbstract ma;

	public Method(Result res, MethodAbstract ma) {
		this.res = res;
		this.ma = ma;
	}

	public ProgramLine[] tranform() {
		List<Integer> ls = res.locs;
		List<Object> cs = res.cons;

		int compId = 0;
		int consId = 0;
		int stateOutId = 0;

		ProgramLine[] progs = null;
		if (ma.isReturnVoid()) {
			progs = new ProgramLine[ma.getWholeProgramSize()];
		} else {
			progs = new ProgramLine[ma.getWholeProgramSize() + 1];
		}
		for (int i = 0; i < ls.size();) {
			Type type = ma.types.get(i);
			if (type.ioType == IOType.STATE_GET) {
				ProgramLine line = new ProgramLine(ProgramLineType.STATE_GET,
						null, new Integer[] { i });
				Integer lineNum = ls.get(i);
				progs[lineNum] = line;
				i++;
			} else if (type.ioType == IOType.FUN_INPUT) {
				ProgramLine line = new ProgramLine(ProgramLineType.INPUT, null,
						new Integer[] { i - ma.getStateSize() });
				Integer lineNum = ls.get(i);
				progs[lineNum] = line;
				i++;
			} else if (type.ioType == IOType.COMP_INPUT
					|| type.ioType == IOType.COMP_OUTPUT) { // components
				Component comp = ma.components.get(compId);
				compId++;

				if (comp.getType() == Components.CONSTANT) {
					Integer lineNum = ls.get(i);
					ProgramLine line = new ProgramLine(
							ProgramLineType.COMPONENT, comp,
							null, cs.get(consId++));
					progs[lineNum] = line;

					i++;
				} else {
					int k = i;
					for (; k < ls.size(); k++) {
						if (ma.types.get(k).ioType == IOType.COMP_OUTPUT) {
							break;
						}
					}
					List<Integer> inputs = ls.subList(i, k);
					Integer lineNum = ls.get(k);
					ProgramLine line = new ProgramLine(
							ProgramLineType.COMPONENT, comp,
							inputs.toArray(new Integer[0]));
					progs[lineNum] = line;

					i = k + 1;
				}
			} else if (type.ioType == IOType.STATE_SET) {
				ProgramLine line = new ProgramLine(ProgramLineType.STATE_SET,
						null, new Integer[] { stateOutId, ls.get(i) });
				progs[ma.getProgramSize() + stateOutId++] = line;
				i++;
			} else if (type.ioType == IOType.FUN_OUTPUT) {
				ProgramLine line = new ProgramLine(ProgramLineType.OUTPUT,
						null, new Integer[] { ls.get(i) });
				progs[ma.getWholeProgramSize()] = line;
				i++;
			}
		}

		return progs;
	}
}
