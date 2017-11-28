package ThirdParty.synthesis.basic;

import java.util.ArrayList;
import java.util.List;

import ThirdParty.synthesis.datadependency.DDPair;

public abstract class Specification {
	public abstract IOPair getIO(Object[] inputs);

	public IOPair getInitIO(List<Type> inputTypes) {
		List<Object> inputs = new ArrayList<Object>();
		for (int i = 0; i < inputTypes.size(); i++) {
			Type type = inputTypes.get(i);
			if (type.varType == VarType.INTEGER) {
				inputs.add(0);
			} else if (type.varType == VarType.DOUBLE) {
				inputs.add(0.0);
			} else if (type.varType == VarType.BOOLEAN) {
				inputs.add(true);
			} else {
				int[] arr = new int[1];
				arr[0] = 1;
				inputs.add(arr);
			}
		}
		return getIO(inputs.toArray());
	}

	public DDPair getInitDDPair(List<Type> inputTypes) {
		return null;
	}
}