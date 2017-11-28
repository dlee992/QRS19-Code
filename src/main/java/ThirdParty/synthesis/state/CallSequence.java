package ThirdParty.synthesis.state;

import java.util.ArrayList;
import java.util.List;

import ThirdParty.synthesis.basic.IOPair;

public class CallSequence {

	public List<Call> calls = new ArrayList<Call>();

	public CallSequence() {
		Call initMC = new Call(ClassAbstract.InitMethodName, new IOPair(
				new int[] {}, null));
		calls.add(initMC);
	}

	public void addCall(Call mc) {
		calls.add(mc);
	}
}
