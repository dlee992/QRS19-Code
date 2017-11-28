package ThirdParty.synthesis.datadependency.test;

import java.util.ArrayList;
import java.util.List;

import ThirdParty.synthesis.basic.IOPair;
import ThirdParty.synthesis.basic.Specification;
import ThirdParty.synthesis.basic.Type;
import ThirdParty.synthesis.component.Component;
import ThirdParty.synthesis.component.ConstantComponent;
import ThirdParty.synthesis.component.IfElseComponent;
import ThirdParty.synthesis.component.LtComponent;
import ThirdParty.synthesis.component.PlusComponent;
import ThirdParty.synthesis.component.StringIndexAtComponent;
import ThirdParty.synthesis.component.StringLengthComponent;
import ThirdParty.synthesis.component.StringN21Component;
import ThirdParty.synthesis.component.StringN2NComponent;
import ThirdParty.synthesis.component.SubStringComponent;
import ThirdParty.synthesis.datadependency.DataDependenceSynthesis;

public class DataDependenceSynthesisTest2 {

	public static void main(String[] args) {
		// max();
		// abs();
		atan();
		// arraycopy();
	}

	public static void max() {

		System.out.println();
		System.out.println("f = if(x<y) return y else return x");

		Specification spec = new Specification() {
			public IOPair getIO(Object[] inputs) {
				int x = (Integer) inputs[0];
				int y = (Integer) inputs[1];
				int output;
				if (x < y) {
					output = y; // no 1, the results are different
				} else {
					output = x;
				}
				return new IOPair(inputs, output);
			}
		};

		// function components
		List<Component> comps = new ArrayList<Component>();
		comps.add(new LtComponent());
		comps.add(new IfElseComponent());

		// function inputs
		List<Type> inputTypes = new ArrayList<Type>();
		inputTypes.add(Type.intType());
		inputTypes.add(Type.intType());

		List<Object[]> repInputs = new ArrayList<Object[]>();
		repInputs.add(new Object[] { 1, 3 });
		repInputs.add(new Object[] { 3, 1 });

		try {
			new DataDependenceSynthesis().doSynthesis(inputTypes, Type.intType(),
					comps, spec, repInputs);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void abs() {

		System.out.println();
		System.out.println("f = abs(x)");

		Specification spec = new Specification() {
			public IOPair getIO(Object[] inputs) {
				int x = (Integer) inputs[0];
				int output = x;
				if (x < 0) {
					output = -x;
				}
				return new IOPair(inputs, output);
			}
		};

		// function components
		List<Component> comps = new ArrayList<Component>();
		comps.add(new ConstantComponent());

		// function inputs
		List<Type> inputTypes = new ArrayList<Type>();
		inputTypes.add(Type.intType());
		List<Object[]> repInputs = new ArrayList<Object[]>();
		repInputs.add(new Object[] { 1 });
		repInputs.add(new Object[] { -1 });

		try {
			new DataDependenceSynthesis().doSynthesis(inputTypes, Type.intType(),
					comps, spec, repInputs);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	
	public static void arraycopy() {

		System.out.println();
		System.out.println("strncpy");

		Specification spec = new Specification() {
			public IOPair getIO(Object[] inputs) {
				int[] arr = (int[]) inputs[0];
				int n = (Integer) inputs[1];

				int max = arr.length < n ? arr.length : n;
				int[] output = new int[max];
				for (int i = 0; i < max; i++) {
					output[i] = arr[i];
				}

				return new IOPair(inputs, output);
			}
		};

		List<Component> comps = new ArrayList<Component>();

		comps.add(new StringLengthComponent());
		comps.add(new StringN21Component());
		comps.add(new StringN2NComponent());
		comps.add(new StringIndexAtComponent());
		comps.add(new SubStringComponent());
		comps.add(new ConstantComponent());

		// function inputs
		List<Type> inputTypes = new ArrayList<Type>();
		inputTypes.add(Type.arrayType());
		inputTypes.add(Type.intType());

		List<IOPair> ioPairs = new ArrayList<IOPair>();

		ioPairs.add(new IOPair(new Object[] { new int[] { 1, 2, 3 }, 3 },
				new int[] { 1, 2, 3 }));
		ioPairs.add(new IOPair(new Object[] { new int[] { 1, 2, 3 }, 2 },
				new int[] { 1, 2 }));

		List<Object[]> repInputs = new ArrayList<Object[]>();
		// repInputs.add(new Object[] { new int[] { 1, 2, 3 }, 6 });
		repInputs.add(new Object[] { new int[] { 4, 5, 6 }, 2 });

		try {
			new DataDependenceSynthesis().doSynthesis(inputTypes, Type.arrayType(),
					comps, spec, repInputs);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void atan() {

		System.out.println();
		System.out.println("f = atan(x, y)");

		Specification spec = new Specification() {
			public IOPair getIO(Object[] inputs) {
				int x = (Integer) inputs[0];
				int y = (Integer) inputs[1];
				int output = x * y;
				return new IOPair(inputs, output);
			}
		};

		// function components
		List<Component> comps = new ArrayList<Component>();
		comps.add(new PlusComponent());

		// function inputs
		List<Type> inputTypes = new ArrayList<Type>();
		inputTypes.add(Type.intType());
		inputTypes.add(Type.intType());

		List<Object[]> repInputs = new ArrayList<Object[]>();
		repInputs.add(new Object[] { 1, 3 });

		try {
			new DataDependenceSynthesis().doSynthesis(inputTypes, Type.intType(),
					comps, spec, repInputs);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}