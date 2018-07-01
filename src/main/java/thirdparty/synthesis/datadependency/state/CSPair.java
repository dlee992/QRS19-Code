package thirdparty.synthesis.datadependency.state;

import thirdparty.synthesis.state.CallSequence;

public class CSPair {
	public CallSequence cs1;
	public CallSequence cs2;

	public CSPair() {
	}

	public CSPair(CallSequence cs1, CallSequence cs2) {
		this.cs1 = cs1;
		this.cs2 = cs2;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof CSPair) {
			CSPair dd = (CSPair) obj;
			if (dd.cs1 == this.cs1 && dd.cs2 == this.cs2) {
				return true;
			}
		}
		return false;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("{");
		sb.append(cs1);
		sb.append(": ");
		sb.append(cs2);
		sb.append("}");
		return sb.toString();
	}
}
