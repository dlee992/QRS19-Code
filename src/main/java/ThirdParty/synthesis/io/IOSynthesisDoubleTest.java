package ThirdParty.synthesis.io;

import java.util.ArrayList;
import java.util.List;

import ThirdParty.synthesis.basic.IOPair;
import ThirdParty.synthesis.basic.Specification;
import ThirdParty.synthesis.basic.Type;
import ThirdParty.synthesis.basic.VarType;
import ThirdParty.synthesis.component.AverageComponent;
import ThirdParty.synthesis.component.Component;
import ThirdParty.synthesis.component.ConstantComponent;
import ThirdParty.synthesis.component.DivComponent;
import ThirdParty.synthesis.component.GEComponent;
import ThirdParty.synthesis.component.IfElseComponent;
import ThirdParty.synthesis.component.PlusComponent;

public class IOSynthesisDoubleTest {

	public static void main(String[] args) {
		// max();
		// average();
		// divDouble();
		// divInteger();
		defaultConstant();
	}

	public static void max() {

		System.err.println();
		System.err.println("f(x, y) = max(x,y)");

		Specification spec = new Specification() {
			public IOPair getIO(Object[] inputs) {
				double x = (Double) inputs[0];
				double y = (Double) inputs[1];
				double output = x;
				if (x < y) {
					output = y;
				}
				return new IOPair(inputs, output);
			}
		};

		// components
		List<Component> comps = new ArrayList<Component>();
		comps.add(new GEComponent(VarType.DOUBLE));
		comps.add(new IfElseComponent(VarType.DOUBLE));

		// function inputs
		List<Type> inputTypes = new ArrayList<Type>();
		inputTypes.add(Type.doubleType());
		inputTypes.add(Type.doubleType());

		try {
			new IOSynthesis().doSynthesis(inputTypes, Type.doubleType(), comps,
					spec);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void average() {

		System.err.println();
		System.err.println("f(x, y) = average(x,y)");

		Specification spec = new Specification() {
			public IOPair getIO(Object[] inputs) {
				double x = (Double) inputs[0];
				double y = (Double) inputs[1];
				double output = (x + y) / 2;
				return new IOPair(inputs, output);
			}
		};

		// components
		List<Component> comps = new ArrayList<Component>();
		comps.add(new AverageComponent(2, VarType.DOUBLE));

		// function inputs
		List<Type> inputTypes = new ArrayList<Type>();
		inputTypes.add(Type.doubleType());
		inputTypes.add(Type.doubleType());

		try {
			new IOSynthesis().doSynthesis(inputTypes, Type.doubleType(), comps,
					spec);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void divDouble() {

		System.err.println();
		System.err.println("f(x, y) = div(x,y)");

		Specification spec = new Specification() {
			public IOPair getIO(Object[] inputs) {
				double x = (Double) inputs[0];
				double y = (Double) inputs[1];
				double output = 0.0;
				if (y != 0) {
					output = x / y;
				}
				return new IOPair(inputs, output);
			}
		};

		// components
		List<Component> comps = new ArrayList<Component>();
		comps.add(new DivComponent(VarType.DOUBLE));
		//comps.add(new PlusComponent(VarType.DOUBLE));

		// function inputs
		List<Type> inputTypes = new ArrayList<Type>();
		inputTypes.add(Type.doubleType());
		inputTypes.add(Type.doubleType());

		try {
			new IOSynthesis().doSynthesis(inputTypes, Type.doubleType(), comps,
					spec);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void divInteger() {

		System.err.println();
		System.err.println("f(x, y) = div(x,y)");

		Specification spec = new Specification() {
			public IOPair getIO(Object[] inputs) {
				int x = (Integer) inputs[0];
				int y = (Integer) inputs[1];
				int output = 0;
				if (y != 0) {
					output = x / y;
				}
				return new IOPair(inputs, output);
			}
		};

		// components
		List<Component> comps = new ArrayList<Component>();
		comps.add(new DivComponent());
		comps.add(new PlusComponent());

		// function inputs
		List<Type> inputTypes = new ArrayList<Type>();
		inputTypes.add(Type.intType());
		inputTypes.add(Type.intType());

		try {
			new IOSynthesis().doSynthesis(inputTypes, Type.intType(), comps,
					spec);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void defaultConstant() {

		System.err.println();
		System.err.println("f(x, y) = div(x,y)");

		Specification spec = new Specification() {
			public IOPair getIO(Object[] inputs) {
				int x = (Integer) inputs[0];
				int y = (Integer) inputs[1];
				int output = 0;
				if (y != 0) {
					output = x / y;
				}
				output += 10;
				return new IOPair(inputs, output);
			}
		};

		// components
		List<Component> comps = new ArrayList<Component>();
		comps.add(new DivComponent());
		comps.add(new PlusComponent());
		comps.add(new ConstantComponent(10));

		// function inputs
		List<Type> inputTypes = new ArrayList<Type>();
		inputTypes.add(Type.intType());
		inputTypes.add(Type.intType());

		try {
			new IOSynthesis().doSynthesis(inputTypes, Type.intType(), comps,
					spec);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
