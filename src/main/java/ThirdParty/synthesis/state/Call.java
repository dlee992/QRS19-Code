package ThirdParty.synthesis.state;

import ThirdParty.synthesis.basic.IOPair;

public class Call {
	public String methodName;
	public IOPair ioPair;

	public Call(String mName, IOPair ioPair) {
		this.methodName = mName;
		this.ioPair = ioPair;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(methodName + ": ");
		sb.append(ioPair);

		return sb.toString();
	}
}
