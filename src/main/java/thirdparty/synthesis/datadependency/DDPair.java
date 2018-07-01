package thirdparty.synthesis.datadependency;

import thirdparty.synthesis.basic.IOPair;

public class DDPair {
	public IOPair io1;
	public IOPair io2;

	public DDPair() {
	}

	public DDPair(IOPair io1, IOPair io2) {
		this.io1 = io1;
		this.io2 = io2;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof DDPair) {
			DDPair dd = (DDPair) obj;
			if (dd.io1 == this.io1 && dd.io2 == this.io2) {
				return true;
			}
		}
		return false;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("{");
		sb.append(io1);
		sb.append(": ");
		sb.append(io2);
		sb.append("}");
		return sb.toString();
	}
}
