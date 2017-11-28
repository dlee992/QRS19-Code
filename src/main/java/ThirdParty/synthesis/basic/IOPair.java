package ThirdParty.synthesis.basic;

public class IOPair {
	public Object[] inputs;
	public Object output;

	public IOPair() {
	}

	public IOPair(Object[] inputs, int output) {
		this.inputs = inputs;
		this.output = output;
	}

	public IOPair(Object[] inputs, Object output) {
		this.inputs = inputs;
		this.output = output;
	}

	public IOPair(Object[] inputs, int[] output) {
		this.inputs = inputs;
		this.output = output;
	}

	public IOPair(int[] inputs, int output) {
		this.inputs = new Integer[inputs.length];
		for (int i = 0; i < inputs.length; i++) {
			this.inputs[i] = inputs[i];
		}
		this.output = output;
	}

	public IOPair(int[] inputs, int[] output) {
		this.inputs = new Integer[inputs.length];
		for (int i = 0; i < inputs.length; i++) {
			this.inputs[i] = inputs[i];
		}
		this.output = output;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof IOPair) {
			IOPair pair = (IOPair) obj;
			for (int i = 0; i < inputs.length; i++) {
				if (inputs[i] == null && pair.inputs[i] != null
						|| !inputs[i].equals(pair.inputs[i])) {
					return false;
				}
			}
			if (output == null && pair.output != null
					|| !output.equals(pair.output)) {
				return false;
			}

			return true;
		}
		return false;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("(<");
		for (Object in : inputs) {
			if (in.getClass().isArray()) {
				int[] arr = (int[]) in;
				sb.append("\"");
				for (int i = 0; i < arr.length; i++) {
					sb.append(arr[i]);
				}
				sb.append("\"");
			} else {
				sb.append(in);
			}
			sb.append(",");
		}
		sb.delete(sb.length() - 1, sb.length());
		sb.append(">, ");
		if (output != null && output.getClass().isArray()) {
			int[] arr = (int[]) output;
			sb.append("\"");
			for (int i = 0; i < arr.length; i++) {
				sb.append(arr[i]);
			}
			sb.append("\"");
		} else {
			sb.append(output);
		}

		sb.append(")");
		return sb.toString();
	}
}
