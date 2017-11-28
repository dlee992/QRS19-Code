package ThirdParty.synthesis.state;

import java.util.List;

import ThirdParty.synthesis.basic.IOType;
import ThirdParty.synthesis.basic.ProgramAbstract;
import ThirdParty.synthesis.basic.Type;
import ThirdParty.synthesis.component.Component;

public class MethodAbstract extends ProgramAbstract {

	public String methodName = null;
	public boolean isInit = false;
	public ClassAbstract classAbstract = null;

	public MethodAbstract(ClassAbstract ca, String mName,
			List<Component> components, List<Type> inputTypes, Type outputType) {

		super(components, inputTypes, outputType);

		this.classAbstract = ca;
		this.methodName = mName;
	}

	public MethodAbstract(String mName, List<Component> components,
			List<Type> inputTypes, Type outputType) {

		super(components, inputTypes, outputType);

		this.methodName = mName;
	}

	public MethodInstance getInstance() {
		return new MethodInstance(this);
	}

	public int getStateSize() {
		return classAbstract.stateAbstract.stateTypes.size();
	}

	public int getInputSize() {
		if (isInit) {
			return 0;
		} else {
			return getStateSize() + inputTypes.size();
		}
	}

	public int getProgramSize() {
		return getInputSize() + components.size();
	}

	public int getWholeProgramSize() {
		return getProgramSize() + getStateSize();
	}

	public List<Type> extractTypes() {

		// input states
		if (this.isInit == false) {
			for (Type type : classAbstract.stateAbstract.stateTypes) {
				types.add(new Type(type.varType, IOType.STATE_GET));
			}
		}

		// program part.
		super.extractTypes();

		// output states
		for (Type type : classAbstract.stateAbstract.stateTypes) {
			types.add(new Type(type.varType, IOType.STATE_SET));
		}

		return types;
	}

}
