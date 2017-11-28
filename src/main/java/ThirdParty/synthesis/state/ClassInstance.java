package ThirdParty.synthesis.state;

import java.util.ArrayList;
import java.util.List;

import com.microsoft.z3.Context;
import com.microsoft.z3.Z3Exception;

public class ClassInstance {

	public ClassAbstract calssAbstract = null;
	public List<MethodInstance> methodInstances = new ArrayList<MethodInstance>();

	public ClassInstance(ClassAbstract ca) {
		this.calssAbstract = ca;
	}

	public void addMethodInstance(MethodInstance mi) {
		methodInstances.add(mi);
	}

	public void init(Context ctx) throws Z3Exception {
		for (MethodInstance mi : methodInstances) {
			mi.init(ctx);
		}
	}
}
