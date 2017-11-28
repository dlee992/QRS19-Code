package ThirdParty.synthesis.basic;

import java.util.List;

import ThirdParty.synthesis.component.Component;
import ThirdParty.synthesis.component.Components;

public class Program {

	private Result res;
	private ProgramAbstract pa;

	public Program(Result res, ProgramAbstract pa) {
		this.res = res;
		this.pa = pa;
	}

	public ProgramLine[] tranform() {
		List<Integer> ls = res.locs;
		List<Object> cs = res.cons;

		int sizeOfProgram = pa.getProgramSize();

		int compId = 0;
		int consId = 0;
		ProgramLine[] progs = new ProgramLine[sizeOfProgram + 1];
		for (int i = 0; i < ls.size();) {
			Type type = pa.types.get(i);
			if (type.ioType == IOType.FUN_INPUT) {
				ProgramLine line = new ProgramLine(ProgramLineType.INPUT, null,
						new Integer[] { i });
				progs[i] = line;
				i++;
			} else if (type.ioType == IOType.FUN_OUTPUT) {
				ProgramLine line = new ProgramLine(ProgramLineType.OUTPUT,
						null, new Integer[] { ls.get(i) });
				progs[sizeOfProgram] = line;
				i++;
			} else { // components
				Component comp = pa.components.get(compId);
				compId++;

				if (comp.getType() == Components.CONSTANT) {
					Integer lineNum = ls.get(i);
					ProgramLine line = new ProgramLine(
							ProgramLineType.COMPONENT, comp, null,
							cs.get(consId++));
					progs[lineNum] = line;

					i++;
				} else {
					int k = i;
					for (; k < ls.size(); k++) {
						if (pa.types.get(k).ioType == IOType.COMP_OUTPUT) {
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
			}
		}

		return progs;
	}
}
