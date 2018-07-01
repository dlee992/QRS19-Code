package thirdparty.synthesis.state;

import java.util.ArrayList;
import java.util.List;

import thirdparty.synthesis.basic.Type;
import thirdparty.synthesis.component.Component;
import thirdparty.synthesis.component.ConstantComponent;

public class ClassDescriptor {

	public static String InitMethodName = "__init";

	public StateAbstract stateAbstract = null;
	private List<MethodAbstract> methods = new ArrayList<MethodAbstract>();

	public ClassDescriptor(StateAbstract stateAbstract) {
		this.stateAbstract = stateAbstract;

		addInitMethod();
	}

	public ClassAbstract getClassAbstract() {
		return new ClassAbstract(this);
	}

	public List<MethodAbstract> getMethods() {
		return methods;
	}

	public void addMethod(String mName, List<Type> inputTypes, Type outputType,
			List<Component> comps) {
		MethodAbstract ma = new MethodAbstract(mName, comps, inputTypes,
				outputType);
		methods.add(ma);
	}

	private void addInitMethod() {

		List<Component> comps = new ArrayList<Component>();
		for (int i = 0; i < stateAbstract.stateTypes.size(); i++) {
			Type type = stateAbstract.stateTypes.get(i);
			comps.add(new ConstantComponent(type.varType));
		}

		MethodAbstract initM = new MethodAbstract(InitMethodName, comps,
				new ArrayList<Type>(), null);
		initM.isInit = true;

		methods.add(initM);
	}
}
