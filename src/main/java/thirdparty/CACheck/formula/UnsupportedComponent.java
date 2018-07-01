package thirdparty.CACheck.formula;

import thirdparty.synthesis.component.Component;

import com.microsoft.z3.Context;
import com.microsoft.z3.Z3Exception;

public final class UnsupportedComponent extends Component {

	public UnsupportedComponent() {
	}

	public void init(Context ctx) throws Z3Exception {
	}

	public String getProg(String[] paras) {
		return null;
	}
}
