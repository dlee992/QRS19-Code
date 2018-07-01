package thirdparty.synthesis.io;

import java.util.ArrayList;
import java.util.List;

import thirdparty.synthesis.basic.IOPair;
import thirdparty.synthesis.basic.Specification;
import thirdparty.synthesis.basic.Type;
import thirdparty.synthesis.component.AndComponent;
import thirdparty.synthesis.component.Component;
import thirdparty.synthesis.component.ConstantComponent;
import thirdparty.synthesis.component.GEComponent;
import thirdparty.synthesis.component.GTComponent;
import thirdparty.synthesis.component.IfElseComponent;
import thirdparty.synthesis.component.LtComponent;
import thirdparty.synthesis.component.MinusComponent;
import thirdparty.synthesis.component.MultComponent;
import thirdparty.synthesis.component.PlusComponent;
import thirdparty.synthesis.component.StringConcatComponent;
import thirdparty.synthesis.component.StringContainComponent;
import thirdparty.synthesis.component.StringIndexAtComponent;
import thirdparty.synthesis.component.StringIndexOfStringComponent;
import thirdparty.synthesis.component.StringLengthComponent;
import thirdparty.synthesis.component.SubStringComponent;

public class IOSynthesisTest {

	public static void main(String[] args) {
		// max();
		// atoi();
		// ifconditon();
		// ifconditon2();
		// complicatedConditions();
		// strlen();
		// strnlen();
		// substring();
		// stringcat();
		// indexat();
		// stringcontain();
		stringindexof();
		// strncpy();
	}

	public static void max() {

		System.err.println();
		System.err.println("f(x, y) = max(x,y)");

		Specification spec = new Specification() {
			public IOPair getIO(Object[] inputs) {
				int x = (Integer) inputs[0];
				int y = (Integer) inputs[1];
				int output = x;
				if (x < y) {
					output = y;
				}
				return new IOPair(inputs, output);
			}
		};

		// components
		List<Component> comps = new ArrayList<Component>();
		comps.add(new GEComponent());
		comps.add(new IfElseComponent());

		// function inputs
		List<Type> inputTypes = new ArrayList<Type>();
		inputTypes.add(Type.intType());
		inputTypes.add(Type.intType());

		try {
			new IOSynthesis().doSynthesis(inputTypes, Type.intType(), comps, spec);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void atoi() {

		System.err.println();
		System.err.println("f(x, y) = 10*x + (y-48)");

		Specification spec = new Specification() {
			public IOPair getIO(Object[] inputs) {
				int x = (Integer) inputs[0];
				int y = (Integer) inputs[1];
				int output = 10 * x + y - 48;
				return new IOPair(inputs, output);
			}
		};

		// components
		List<Component> comps = new ArrayList<Component>();
		comps.add(new ConstantComponent());
		comps.add(new ConstantComponent());
		comps.add(new PlusComponent());
		comps.add(new MultComponent());
		comps.add(new MinusComponent());

		// function inputs
		List<Type> inputTypes = new ArrayList<Type>();
		inputTypes.add(Type.intType());
		inputTypes.add(Type.intType());

		try {
			new IOSynthesis().doSynthesis(inputTypes, Type.intType(), comps, spec);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void ifconditon() {

		System.err.println();
		System.err.println("f = if(x<10) return y else return 10");

		Specification spec = new Specification() {
			public IOPair getIO(Object[] inputs) {
				int x = (Integer) inputs[0];
				int y = (Integer) inputs[1];
				int output;
				if (x < 10) {
					output = y;
				} else {
					output = 10;
				}
				return new IOPair(inputs, output);
			}
		};

		// function components
		List<Component> comps = new ArrayList<Component>();
		comps.add(new ConstantComponent());
		comps.add(new GTComponent());
		comps.add(new IfElseComponent());

		// function inputs
		List<Type> inputTypes = new ArrayList<Type>();
		inputTypes.add(Type.intType());
		inputTypes.add(Type.intType());

		// List<IOPair> ioPairs = new ArrayList<IOPair>();
		// ioPairs.add(new IOPair(new int[] { 1, 2 }, 2));
		// ioPairs.add(new IOPair(new int[] { 0, -1 }, -1));
		// ioPairs.add(new IOPair(new int[] { 3, 4 }, 4));
		// ioPairs.add(new IOPair(new int[] { -2, -3 }, -3));
		// ioPairs.add(new IOPair(new int[] { -5, -5 }, -5));
		// ioPairs.add(new IOPair(new int[] { -7, -7 }, -7));
		// ioPairs.add(new IOPair(new int[] { 6, 7 }, 7));
		// ioPairs.add(new IOPair(new int[] { 8, 8 }, 8));
		// ioPairs.add(new IOPair(new int[] { -10, -9 }, -9));
		// ioPairs.add(new IOPair(new int[] { 8, 7 }, 7));
		// ioPairs.add(new IOPair(new int[] { 9, 8 }, 8));
		// ioPairs.add(new IOPair(new int[] { -11, -10 }, -10));
		// ioPairs.add(new IOPair(new int[] { -12, -11 }, -11));
		// ioPairs.add(new IOPair(new int[] { 9, 11 }, 11));
		// ioPairs.add(new IOPair(new int[] { 11, 11 }, 10));
		// ioPairs.add(new IOPair(new int[] { 10, 9 }, 10));

		try {
			new IOSynthesis().doSynthesis(inputTypes, Type.intType(), comps, spec);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void ifconditon2() {

		System.err.println();
		System.err.println("f = if(x<100) return 0 else return 1");

		Specification spec = new Specification() {
			public IOPair getIO(Object[] inputs) {
				int x = (Integer) inputs[0];
				int output;
				if (x < 100) {
					output = 0;
				} else {
					output = 1;
				}
				return new IOPair(inputs, output);
			}
		};

		// function components
		List<Component> comps = new ArrayList<Component>();
		comps.add(new ConstantComponent());
		comps.add(new ConstantComponent());
		comps.add(new ConstantComponent());
		comps.add(new GTComponent());
		comps.add(new IfElseComponent());

		// function inputs
		List<Type> inputTypes = new ArrayList<Type>();
		inputTypes.add(Type.intType());

		List<IOPair> ioPairs = new ArrayList<IOPair>();
		ioPairs.add(new IOPair(new int[] { 99 }, 0));
		ioPairs.add(new IOPair(new int[] { 100 }, 1));

		try {
			new IOSynthesis().doSynthesis(inputTypes, Type.intType(), comps, spec);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void complicatedConditions() {

		System.err.println();
		System.err.println("f = if (a>0 && b>0) return a+b else return a-b");

		Specification spec = new Specification() {
			public IOPair getIO(Object[] inputs) {
				int x = (Integer) inputs[0];
				int y = (Integer) inputs[1];
				int output;
				if (x > 0 && y > 0) {
					output = x + y;
				} else {
					output = x - y;
				}
				return new IOPair(inputs, output);
			}
		};

		// components
		List<Component> comps = new ArrayList<Component>();
		comps.add(new GTComponent());
		comps.add(new GTComponent());
		comps.add(new AndComponent());
		// comps.add(new ConstantComponent());
		comps.add(new IfElseComponent());
		comps.add(new PlusComponent());
		comps.add(new MinusComponent());

		// function inputs
		List<Type> inputTypes = new ArrayList<Type>();
		inputTypes.add(Type.intType());
		inputTypes.add(Type.intType());

		try {
			new IOSynthesis().doSynthesis(inputTypes, Type.intType(), comps, spec);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void strlen() {

		System.err.println();
		System.err.println("f = length(arr)+b");

		Specification spec = new Specification() {
			public IOPair getIO(Object[] inputs) {
				int[] x = (int[]) inputs[0];
				int y = (Integer) inputs[1];
				int output = x.length + y;
				return new IOPair(inputs, output);
			}
		};

		List<Component> comps = new ArrayList<Component>();
		comps.add(new StringLengthComponent());
		comps.add(new PlusComponent());

		// function inputs
		List<Type> inputTypes = new ArrayList<Type>();
		inputTypes.add(Type.arrayType());
		inputTypes.add(Type.intType());

		Type outputType = Type.intType();

		List<IOPair> ioPairs = new ArrayList<IOPair>();

		ioPairs.add(new IOPair(new Object[] { new int[] { 1, 2, 3 }, 3 }, 6));
		ioPairs.add(new IOPair(new Object[] { new int[] {}, 1 }, 1));

		try {
			new IOSynthesis().doSynthesis(inputTypes, outputType, comps, spec);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void strnlen() {

		System.err.println();
		System.err.println("f = strnlen(arr, maxlen)");

		Specification spec = new Specification() {
			public IOPair getIO(Object[] inputs) {
				int[] x = (int[]) inputs[0];
				int y = (Integer) inputs[1];
				int output = x.length;
				if (x.length > y) {
					output = y;
				}

				return new IOPair(inputs, output);
			}
		};

		// component
		List<Component> comps = new ArrayList<Component>();
		comps.add(new IfElseComponent());
		comps.add(new GTComponent());
		comps.add(new StringLengthComponent());

		// function inputs
		List<Type> inputTypes = new ArrayList<Type>();
		inputTypes.add(Type.arrayType());
		inputTypes.add(Type.intType());

		Type outputType = Type.intType();

		List<IOPair> ioPairs = new ArrayList<IOPair>();

		ioPairs.add(new IOPair(new Object[] { new int[] { 1, 2, 3 }, 5 }, 3));
		ioPairs.add(new IOPair(new Object[] { new int[] { 1, 2, 3 }, 2 }, 2));
		ioPairs.add(new IOPair(new Object[] { new int[] { 1, 2, 3 }, 3 }, 3));

		try {
			new IOSynthesis().doSynthesis(inputTypes, outputType, comps, spec);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void substring() {

		System.err.println();
		System.err.println("f = substring(arr, i, j)");

		Specification spec = new Specification() {
			public IOPair getIO(Object[] inputs) {
				int[] arr = (int[]) inputs[0];
				int i = (Integer) inputs[1];
				int j = (Integer) inputs[2];

				int[] output;
				if (i < j) {
					output = new int[j - i];
					for (int k = i; k < j; k++) {
						output[k - i] = arr[k];
					}
				} else {
					output = new int[0];
				}

				return new IOPair(inputs, output);
			}
		};

		List<Component> comps = new ArrayList<Component>();
		comps.add(new SubStringComponent());

		// function inputs
		List<Type> inputTypes = new ArrayList<Type>();
		inputTypes.add(Type.arrayType());
		inputTypes.add(Type.intType());
		inputTypes.add(Type.intType());

		List<IOPair> ioPairs = new ArrayList<IOPair>();

		ioPairs.add(new IOPair(new Object[] { new int[] { 1, 2, 3 }, 0, 1 },
				new int[] { 1 }));
		ioPairs.add(new IOPair(new Object[] { new int[] { 1, 2, 3 }, 1, 3 },
				new int[] { 2, 3 }));

		try {
			new IOSynthesis().doSynthesis(inputTypes, Type.arrayType(), comps, spec);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void stringcat() {

		System.err.println();
		System.err.println("f = stringconcat(arr1, arr2)");

		Specification spec = new Specification() {
			public IOPair getIO(Object[] inputs) {
				int[] arr1 = (int[]) inputs[0];
				int[] arr2 = (int[]) inputs[1];

				int[] output = new int[arr1.length + arr2.length];
				int k = 0;
				for (; k < arr1.length; k++) {
					output[k] = arr1[k];
				}
				for (; k < arr1.length + arr2.length; k++) {
					output[k] = arr2[k - arr1.length];
				}

				return new IOPair(inputs, output);
			}
		};

		List<Component> comps = new ArrayList<Component>();
		comps.add(new StringConcatComponent());

		// function inputs
		List<Type> inputTypes = new ArrayList<Type>();
		inputTypes.add(Type.arrayType());
		inputTypes.add(Type.arrayType());

		List<IOPair> ioPairs = new ArrayList<IOPair>();

		ioPairs.add(new IOPair(new Object[] { new int[] { 1, 2, 3 },
				new int[] { 4 } }, new int[] { 1, 2, 3, 4 }));

		try {
			new IOSynthesis().doSynthesis(inputTypes, Type.arrayType(), comps, spec);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void indexat() {

		System.err.println();
		System.err.println("f = indexat(arr, i)");

		Specification spec = new Specification() {
			public IOPair getIO(Object[] inputs) {
				int[] arr = (int[]) inputs[0];
				int i = (Integer) inputs[1];

				int output = arr[i];

				return new IOPair(inputs, output);
			}
		};

		List<Component> comps = new ArrayList<Component>();
		comps.add(new StringIndexAtComponent());
		// comps.add(new PlusComponent());

		// function inputs
		List<Type> inputTypes = new ArrayList<Type>();
		inputTypes.add(Type.arrayType());
		inputTypes.add(Type.intType());

		List<IOPair> ioPairs = new ArrayList<IOPair>();

		ioPairs.add(new IOPair(new Object[] { new int[] { 1, 2, 3 }, 2 }, 3));

		try {
			new IOSynthesis().doSynthesis(inputTypes, Type.intType(), comps, spec);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void stringcontain() {

		System.err.println();
		System.err.println("f = stringcontains(arr1, arr2)");

		List<Component> comps = new ArrayList<Component>();
		comps.add(new StringContainComponent());

		Specification spec = new Specification() {
			public IOPair getIO(Object[] inputs) {
				int[] arr1 = (int[]) inputs[0];
				int[] arr2 = (int[]) inputs[1];

				boolean output = Boolean.FALSE;
				for (int i = 0; i < arr1.length; i++) {
					int j = 0;
					for (; j < arr2.length && i + j < arr1.length; j++) {
						if (arr1[i + j] != arr2[j]) {
							break;
						}
					}
					if (j == arr2.length) {
						output = Boolean.TRUE;
						break;
					}
				}

				if (arr1.length == 0 || arr2.length == 0) {
					output = Boolean.FALSE;
				}

				return new IOPair(inputs, output);
			}
		};

		// function inputs
		List<Type> inputTypes = new ArrayList<Type>();
		inputTypes.add(Type.arrayType());
		inputTypes.add(Type.arrayType());

		List<IOPair> ioPairs = new ArrayList<IOPair>();

		ioPairs.add(new IOPair(new Object[] { new int[] { 1, 2, 3 },
				new int[] { 3 } }, Boolean.TRUE));
		ioPairs.add(new IOPair(new Object[] { new int[] { 1, 2, 3 },
				new int[] { 4, 5 } }, Boolean.FALSE));

		try {
			new IOSynthesis().doSynthesis(inputTypes, Type.boolType(), comps, spec);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void stringindexof() {

		System.err.println();
		System.err.println("f = stringindexof(arr1, arr2)");

		Specification spec = new Specification() {
			public IOPair getIO(Object[] inputs) {
				int[] arr1 = (int[]) inputs[0];
				int[] arr2 = (int[]) inputs[1];

				int output = -1;
				for (int i = 0; i < arr1.length; i++) {
					int j = 0;
					for (; j < arr2.length && i + j < arr1.length; j++) {
						if (arr1[i + j] != arr2[j]) {
							break;
						}
					}
					if (j == arr2.length) {
						output = i;
						break;
					}
				}

				if (arr1.length == 0 || arr2.length == 0) {
					output = -1;
				}

				return new IOPair(inputs, output);
			}
		};

		List<Component> comps = new ArrayList<Component>();
		comps.add(new StringIndexOfStringComponent());

		// function inputs
		List<Type> inputTypes = new ArrayList<Type>();
		inputTypes.add(Type.arrayType());
		inputTypes.add(Type.arrayType());

		Type outputType = Type.intType();

		List<IOPair> ioPairs = new ArrayList<IOPair>();

		ioPairs.add(new IOPair(new Object[] { new int[] { 1, 2, 3 },
				new int[] { 1 } }, 0));
		ioPairs.add(new IOPair(new Object[] { new int[] { 1, 2, 3 },
				new int[] { 2 } }, 1));
		ioPairs.add(new IOPair(new Object[] { new int[] { 1, 2, 3 },
				new int[] { 4 } }, -1));

		try {
			new IOSynthesis().doSynthesis(inputTypes, outputType, comps, spec);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void strncpy() {

		System.err.println();
		System.err.println("f = strncpy(arr, maxlen)");

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

		// component
		List<Component> comps = new ArrayList<Component>();
		comps.add(new IfElseComponent());
		comps.add(new LtComponent());
		comps.add(new ConstantComponent());
		comps.add(new StringLengthComponent());
		comps.add(new SubStringComponent());

		// function inputs
		List<Type> inputTypes = new ArrayList<Type>();
		inputTypes.add(Type.arrayType());
		inputTypes.add(Type.intType());

		Type outputType = Type.arrayType();

		List<IOPair> ioPairs = new ArrayList<IOPair>();

		ioPairs.add(new IOPair(new Object[] { new int[] { 1 }, 0 },
				new int[] {}));
		ioPairs.add(new IOPair(new Object[] { new int[] { 9 }, 1 },
				new int[] { 9 }));
		ioPairs.add(new IOPair(new Object[] {
				new int[] { 6, 1, 1, 1, 5, 5, 6, 7 }, 7 }, new int[] { 6, 1, 1,
				1, 5, 5, 6 }));
		ioPairs.add(new IOPair(new Object[] {
				new int[] { 6, 1, 1, 1, 5, 5, 6, 7 }, 9 }, new int[] { 6, 1, 1,
				1, 5, 5, 6, 7 }));

		try {
			new IOSynthesis().doSynthesis(inputTypes, outputType, comps, spec);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
