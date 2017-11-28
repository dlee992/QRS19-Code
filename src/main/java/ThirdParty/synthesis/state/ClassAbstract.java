package ThirdParty.synthesis.state;

import java.util.ArrayList;
import java.util.List;

import com.microsoft.z3.Context;
import com.microsoft.z3.Z3Exception;

public class ClassAbstract {

	public static String InitMethodName = "__init";

	public StateAbstract stateAbstract = null;
	public List<MethodAbstract> methods = new ArrayList<MethodAbstract>();

	public ClassAbstract(ClassDescriptor cd) {
		this.stateAbstract = cd.stateAbstract;

		for (MethodAbstract m : cd.getMethods()) {
			MethodAbstract ma = new MethodAbstract(this, m.methodName,
					m.components, m.inputTypes, m.outputType);
			ma.isInit = m.isInit;

			methods.add(ma);
		}
	}

	public void init(Context ctx) throws Z3Exception {
		for (MethodAbstract ma : methods) {
			ma.init(ctx);
		}
	}

	public ClassInstance getInstance(List<Call> methodCalls) {
		ClassInstance ci = new ClassInstance(this);

		for (Call call : methodCalls) {
			MethodAbstract ma = getMethod(call.methodName);
			MethodInstance mi = ma.getInstance();
			ci.addMethodInstance(mi);
		}

		return ci;
	}

	public MethodAbstract getMethod(String mName) {
		for (MethodAbstract ma : methods) {
			if (ma.methodName.equals(mName)) {
				return ma;
			}
		}
		return null;
	}
}
