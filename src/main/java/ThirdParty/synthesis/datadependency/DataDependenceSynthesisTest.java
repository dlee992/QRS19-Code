package ThirdParty.synthesis.datadependency;

import java.util.ArrayList;
import java.util.List;

import ThirdParty.synthesis.basic.IOPair;
import ThirdParty.synthesis.basic.Specification;
import ThirdParty.synthesis.basic.Type;
import ThirdParty.synthesis.component.AndComponent;
import ThirdParty.synthesis.component.Component;
import ThirdParty.synthesis.component.ConstantComponent;
import ThirdParty.synthesis.component.EqualComponent;
import ThirdParty.synthesis.component.GTComponent;
import ThirdParty.synthesis.component.IfElseComponent;
import ThirdParty.synthesis.component.LtComponent;
import ThirdParty.synthesis.component.PlusComponent;
import ThirdParty.synthesis.component.StringConcatComponent;
import ThirdParty.synthesis.component.StringContainComponent;
import ThirdParty.synthesis.component.StringIndexAtComponent;
import ThirdParty.synthesis.component.StringIndexOfStringComponent;
import ThirdParty.synthesis.component.StringLengthComponent;
import ThirdParty.synthesis.component.StringN21Component;
import ThirdParty.synthesis.component.StringN2NComponent;
import ThirdParty.synthesis.component.SubStringComponent;

public class DataDependenceSynthesisTest {

	public static void main(String[] args) {
		// simple();
		// simple1();
		// simple2();
		// simple3();
		// simple4();
		// extremecase();
		// ifcondition();
		// ifcondition2();
		// ifcondition31();
		// ifcondition3();
		// ifcondition4();
		// ifcondition5();
		// strlen();
		// strnlen();
		// strcmp();
		// strcontain();
		// strcpy();
		strncpy();
		// stringcat();
	}

	public static void simple() {

		System.out.println();
		System.out.println("f(x, y) = y");

		Specification spec = new Specification() {
			public IOPair getIO(Object[] inputs) {
				int y = (Integer) inputs[1];
				int output = y;
				return new IOPair(inputs, output);
			}
		};

		// function components
		List<Component> comps = new ArrayList<Component>();
		comps.add(new PlusComponent());
		comps.add(new ConstantComponent());

		// function inputs
		List<Type> inputTypes = new ArrayList<Type>();
		inputTypes.add(Type.intType());
		inputTypes.add(Type.intType());

		try {
			new DataDependenceSynthesis().doSynthesis(inputTypes, Type.intType(),
					comps, spec);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void simple1() {

		System.out.println();
		System.out.println("f(x, y) = x^2");

		Specification spec = new Specification() {
			public IOPair getIO(Object[] inputs) {
				int x = (Integer) inputs[0];
				// int y = inputs[1];
				int output = x * x;
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

		try {
			new DataDependenceSynthesis().doSynthesis(inputTypes, Type.intType(),
					comps, spec);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void simple2() {

		System.out.println();
		System.out.println("f(x, y) = x^2 + y^2");

		Specification spec = new Specification() {
			public IOPair getIO(Object[] inputs) {
				int x = (Integer) inputs[0];
				int y = (Integer) inputs[1];
				int output = x * x + y * y;
				return new IOPair(inputs, output);
			}
		};

		// function components
		List<Component> comps = new ArrayList<Component>();
		comps.add(new PlusComponent());
		// comps.add(new AbsComponent());
		// comps.add(new AbsComponent());

		// function inputs
		List<Type> inputTypes = new ArrayList<Type>();
		inputTypes.add(Type.intType());
		inputTypes.add(Type.intType());

		try {
			new DataDependenceSynthesis().doSynthesis(inputTypes, Type.intType(),
					comps, spec);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void simple3() {

		System.out.println();
		System.out.println("f(x, y) = x^3 + y^3");

		Specification spec = new Specification() {
			public IOPair getIO(Object[] inputs) {
				int x = (Integer) inputs[0];
				int y = (Integer) inputs[1];
				int output = x * x * x + y * y * y;
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

		try {
			new DataDependenceSynthesis().doSynthesis(inputTypes, Type.intType(),
					comps, spec);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void simple4() {

		System.out.println();
		System.out.println("f(x, y) = x/2");

		Specification spec = new Specification() {
			public IOPair getIO(Object[] inputs) {
				int x = (Integer) inputs[0];
				int output = x / 2;
				return new IOPair(inputs, output);
			}
		};

		// function components
		List<Component> comps = new ArrayList<Component>();
		comps.add(new PlusComponent());
		comps.add(new ConstantComponent());

		// function inputs
		List<Type> inputTypes = new ArrayList<Type>();
		inputTypes.add(Type.intType());
		inputTypes.add(Type.intType());

		try {
			new DataDependenceSynthesis().doSynthesis(inputTypes, Type.intType(),
					comps, spec);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void extremecase() {

		System.out.println();
		System.out.println("f(x, y) = 10");

		Specification spec = new Specification() {
			public IOPair getIO(Object[] inputs) {
				return new IOPair(inputs, 10);
			}
		};

		// function components
		List<Component> comps = new ArrayList<Component>();
		comps.add(new ConstantComponent());
		comps.add(new PlusComponent());

		// function inputs
		List<Type> inputTypes = new ArrayList<Type>();
		inputTypes.add(Type.intType());
		inputTypes.add(Type.intType());

		List<IOPair> ioPairs = new ArrayList<IOPair>();
		ioPairs.add(new IOPair(new int[] { 1, 2 }, 10));
		ioPairs.add(new IOPair(new int[] { 1, 1 }, 10));
		ioPairs.add(new IOPair(new int[] { -4, 0 }, 10));
		ioPairs.add(new IOPair(new int[] { 0, 0 }, 10));

		try {
			new DataDependenceSynthesis().doSynthesis(inputTypes, Type.intType(),
					comps, spec);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void ifcondition() {

		System.out.println();
		System.out.println("f = if(x<10) return y else return 10");

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
		// comps.add(new ConstantComponent());
		comps.add(new GTComponent());
		comps.add(new IfElseComponent());

		// function inputs
		List<Type> inputTypes = new ArrayList<Type>();
		inputTypes.add(Type.intType());
		inputTypes.add(Type.intType());

		try {
			new DataDependenceSynthesis().doSynthesis(inputTypes, Type.intType(),
					comps, spec);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void ifcondition2() {

		System.out.println();
		System.out.println("f = if(y<10) return x else return -x");

		Specification spec = new Specification() {
			public IOPair getIO(Object[] inputs) {
				int x = (Integer) inputs[0];
				int y = (Integer) inputs[1];
				int output;
				if (y < 10) {
					output = x;
				} else {
					output = -x;
				}
				return new IOPair(inputs, output);
			}
		};

		// function components
		List<Component> comps = new ArrayList<Component>();
		comps.add(new ConstantComponent());
		// comps.add(new ConstantComponent());
		comps.add(new GTComponent());
		// comps.add(new IfElseComponent());

		// function inputs
		List<Type> inputTypes = new ArrayList<Type>();
		inputTypes.add(Type.intType());
		inputTypes.add(Type.intType());

		try {
			new DataDependenceSynthesis().doSynthesis(inputTypes, Type.intType(),
					comps, spec);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void ifcondition31() {

		System.out.println();
		System.out.println("f = if(x<y) return x else return y");

		Specification spec = new Specification() {
			public IOPair getIO(Object[] inputs) {
				int x = (Integer) inputs[0];
				int y = (Integer) inputs[1];
				int output;
				if (x < y) {
					output = x; // no 1, the results are different
				} else {
					output = y;
				}
				return new IOPair(inputs, output);
			}
		};

		// function components
		List<Component> comps = new ArrayList<Component>();
		// comps.add(new ConstantComponent());
		// comps.add(new ConstantComponent());
		comps.add(new LtComponent());
		comps.add(new IfElseComponent());

		// function inputs
		List<Type> inputTypes = new ArrayList<Type>();
		inputTypes.add(Type.intType());
		inputTypes.add(Type.intType());

		List<IOPair> ioPairs = new ArrayList<IOPair>();
		ioPairs.add(new IOPair(new int[] { -1, 0 }, -1));
		ioPairs.add(new IOPair(new int[] { -2, 0 }, -2));
		ioPairs.add(new IOPair(new int[] { 1, -1 }, -1));
		ioPairs.add(new IOPair(new int[] { 0, -1 }, -1));

		ioPairs.add(new IOPair(new int[] { 8, 8 }, 8));
		ioPairs.add(new IOPair(new int[] { 8, 7 }, 7));
		// ioPairs.add(new IOPair(new int[] { 8, 9 }, 8));

		try {
			new DataDependenceSynthesis().doSynthesis(inputTypes, Type.intType(),
					comps, spec);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void ifcondition3() {

		System.out.println();
		System.out.println("f = if(x<y) return x else return y*y+1");

		Specification spec = new Specification() {
			public IOPair getIO(Object[] inputs) {
				int x = (Integer) inputs[0];
				int y = (Integer) inputs[1];
				int output;
				if (x < y) {
					output = x; // no 1, the results are different
				} else {
					output = y * y + 1;
				}
				return new IOPair(inputs, output);
			}
		};

		// function components
		List<Component> comps = new ArrayList<Component>();
		// comps.add(new ConstantComponent());
		// comps.add(new ConstantComponent());
		comps.add(new LtComponent());
		comps.add(new IfElseComponent());
		comps.add(new IfElseComponent());

		// function inputs
		List<Type> inputTypes = new ArrayList<Type>();
		inputTypes.add(Type.intType());
		inputTypes.add(Type.intType());

		List<IOPair> ioPairs = new ArrayList<IOPair>();
		ioPairs.add(new IOPair(new int[] { 0, 0 }, 1));
		ioPairs.add(new IOPair(new int[] { 1, 0 }, 1));
		ioPairs.add(new IOPair(new int[] { 0, 1 }, 2));

		try {
			new DataDependenceSynthesis().doSynthesis(inputTypes, Type.intType(),
					comps, spec);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void ifcondition4() {

		System.out.println();
		System.out.println("f = if(x<y) return x+y else return x*x+y*y");

		Specification spec = new Specification() {
			public IOPair getIO(Object[] inputs) {
				int x = (Integer) inputs[0];
				int y = (Integer) inputs[1];
				int output;
				if (x < y) {
					output = x + y; // no 1, the results are different
				} else {
					output = x * x + y * y;
				}
				return new IOPair(inputs, output);
			}
		};

		// function components
		List<Component> comps = new ArrayList<Component>();
		// comps.add(new ConstantComponent());
		comps.add(new PlusComponent());
		comps.add(new LtComponent());
		comps.add(new IfElseComponent());

		// function inputs
		List<Type> inputTypes = new ArrayList<Type>();
		inputTypes.add(Type.intType());
		inputTypes.add(Type.intType());

		try {
			new DataDependenceSynthesis().doSynthesis(inputTypes, Type.intType(),
					comps, spec);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void ifcondition5() {

		System.out.println();
		System.out.println("f = if(x<y) return x+y else return x*x");

		Specification spec = new Specification() {
			public IOPair getIO(Object[] inputs) {
				int x = (Integer) inputs[0];
				int y = (Integer) inputs[1];
				int output;
				if (x < y) {
					output = x + y; // no 1, the results are different
				} else {
					output = x * x;
				}
				return new IOPair(inputs, output);
			}
		};

		// function components
		List<Component> comps = new ArrayList<Component>();
		// comps.add(new ConstantComponent());
		comps.add(new PlusComponent());
		comps.add(new LtComponent());
		comps.add(new IfElseComponent());

		// function inputs
		List<Type> inputTypes = new ArrayList<Type>();
		inputTypes.add(Type.intType());
		inputTypes.add(Type.intType());

		try {
			new DataDependenceSynthesis().doSynthesis(inputTypes, Type.intType(),
					comps, spec);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void strlen() {

		System.out.println();
		System.out.println("strlen");

		Specification spec = new Specification() {
			public IOPair getIO(Object[] inputs) {
				int[] x = (int[]) inputs[0];
				return new IOPair(inputs, x.length);
			}
		};

		// function components
		List<Component> comps = new ArrayList<Component>();
		// comps.add(new StringLengthComponent());
		// comps.add(new ConstantComponent());
		// comps.add(new StringIndexAtComponent());

		comps.add(new StringLengthComponent());
		comps.add(new StringN21Component());
		comps.add(new StringN2NComponent());
		comps.add(new StringIndexAtComponent());
		comps.add(new SubStringComponent());
		comps.add(new ConstantComponent());

		// function inputs
		List<Type> inputTypes = new ArrayList<Type>();
		inputTypes.add(Type.arrayType());

		Type outputType = Type.intType();

		List<IOPair> ioPairs = new ArrayList<IOPair>();
		ioPairs.add(new IOPair(new Object[] { new int[] { 1, 2, 3 } }, 3));
		ioPairs.add(new IOPair(new Object[] { new int[] { 1, 2 } }, 2));

		List<Object[]> repInputs = new ArrayList<Object[]>();
		repInputs.add(new Object[] { new int[] { 1, 2, 3 } });

		try {
			new DataDependenceSynthesis().doSynthesis(inputTypes, outputType, comps,
					spec, repInputs);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void strnlen() {

		System.out.println();
		System.out.println("strnlen");

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

		// function components
		List<Component> comps = new ArrayList<Component>();
		comps.add(new IfElseComponent());
		comps.add(new LtComponent());

		comps.add(new StringLengthComponent());
		comps.add(new StringN21Component());
		comps.add(new StringN2NComponent());
		comps.add(new StringIndexAtComponent());
		comps.add(new SubStringComponent());
		// comps.add(new ConstantComponent());

		// function inputs
		List<Type> inputTypes = new ArrayList<Type>();
		inputTypes.add(Type.arrayType());
		inputTypes.add(Type.intType());

		Type outputType = Type.intType();

		List<IOPair> ioPairs = new ArrayList<IOPair>();
		ioPairs.add(new IOPair(new Object[] { new int[] { 1, 2, 3 }, 4 }, 3));
		ioPairs.add(new IOPair(new Object[] { new int[] { 1, 2, 3 }, 3 }, 3));
		ioPairs.add(new IOPair(new Object[] { new int[] { 1, 2, 3 }, 2 }, 2));
		ioPairs.add(new IOPair(new Object[] { new int[] { 1, 2 }, 2 }, 2));

		List<Object[]> repInputs = new ArrayList<Object[]>();
		repInputs.add(new Object[] { new int[] { 1, 2, 3 }, 5 });
		repInputs.add(new Object[] { new int[] { 4, 5, 6, 7 }, 2 });

		try {
			new DataDependenceSynthesis().doSynthesis(inputTypes, outputType, comps,
					spec, repInputs);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void strcmp() {

		System.out.println();
		System.out.println("strcmp");

		Specification spec = new Specification() {
			public IOPair getIO(Object[] inputs) {
				int[] arr1 = (int[]) inputs[0];
				int[] arr2 = (int[]) inputs[1];

				int output = 0;
				for (int i = 0; i < arr1.length && i < arr2.length; i++) {
					if (arr1[i] != arr2[i]) {
						output = arr1[i] - arr2[i];
						break;
					}
				}

				if (output == 0) {
					if (arr1.length > arr2.length) {
						output = arr1[arr2.length];
					} else if (arr1.length < arr2.length) {
						output = 0 - arr2[arr1.length];
					}
				}

				if (output != 0)
					output = 1;

				return new IOPair(inputs, output);
			}
		};

		// function components
		List<Component> comps = new ArrayList<Component>();
		comps.add(new StringIndexOfStringComponent());
		comps.add(new StringIndexOfStringComponent());
		comps.add(new PlusComponent());

		comps.add(new IfElseComponent());
		comps.add(new EqualComponent());
		comps.add(new EqualComponent());
		comps.add(new ConstantComponent());
		comps.add(new AndComponent());

		// function inputs
		List<Type> inputTypes = new ArrayList<Type>();
		inputTypes.add(Type.arrayType());
		inputTypes.add(Type.arrayType());

		Type outputType = Type.intType();

		List<IOPair> ioPairs = new ArrayList<IOPair>();
		ioPairs.add(new IOPair(new Object[] { new int[] { 1, 2, 3 },
				new int[] { 1, 2, 3 } }, 0));
		ioPairs.add(new IOPair(new Object[] { new int[] { 1, 2, 3 },
				new int[] { 1, 2 } }, 1));
		ioPairs.add(new IOPair(new Object[] { new int[] { 1, 2, 3 },
				new int[] { 1, 1 } }, 1));

		List<Object[]> repInputs = new ArrayList<Object[]>();
		repInputs
				.add(new Object[] { new int[] { 1, 2, 3 }, new int[] { 4, 5 } });
		repInputs.add(new Object[] { new int[] { 6, 7, 8 },
				new int[] { 6, 7, 8 } });

		try {
			new DataDependenceSynthesis().doSynthesis(inputTypes, outputType, comps,
					spec, repInputs);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void strcontain() {

		System.out.println();
		System.out.println("strcontain");

		Specification spec = new Specification() {
			public IOPair getIO(Object[] inputs) {
				int[] arr1 = (int[]) inputs[0];
				int[] arr2 = (int[]) inputs[1];

				boolean ret = false;

				for (int i = 0; i <= arr1.length - arr2.length; i++) {
					int j = 0;
					for (; j < arr2.length; j++) {
						if (arr1[i + j] != arr2[j]) {
							break;
						}
					}
					if (j == arr2.length) {
						ret = true;
						break;
					}
				}

				return new IOPair(inputs, ret);
			}
		};

		// function components
		List<Component> comps = new ArrayList<Component>();
		comps.add(new StringContainComponent());
		comps.add(new StringConcatComponent());
		comps.add(new StringIndexOfStringComponent());
		comps.add(new StringLengthComponent());
		comps.add(new StringIndexAtComponent());

		// function inputs
		List<Type> inputTypes = new ArrayList<Type>();
		inputTypes.add(Type.arrayType());
		inputTypes.add(Type.arrayType());

		List<Object[]> repInputs = new ArrayList<Object[]>();
		repInputs
				.add(new Object[] { new int[] { 1, 2, 3 }, new int[] { 1, 2 } });
		repInputs
				.add(new Object[] { new int[] { 6, 7, 8 }, new int[] { 4, 5 } });

		try {
			new DataDependenceSynthesis().doSynthesis(inputTypes, Type.boolType(),
					comps, spec, repInputs);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void strcpy() {

		System.out.println();
		System.out.println("strcpy");

		Specification spec = new Specification() {
			public IOPair getIO(Object[] inputs) {
				int[] arr = (int[]) inputs[0];

				return new IOPair(inputs, arr);
			}
		};

		// function components
		List<Component> comps = new ArrayList<Component>();
		// comps.add(new ConstantComponent());
		// comps.add(new StringLengthComponent());
		// comps.add(new SubStringComponent());

		comps.add(new StringLengthComponent());
		comps.add(new StringN21Component());
		comps.add(new StringN2NComponent());
		comps.add(new StringIndexAtComponent());
		comps.add(new SubStringComponent());

		// function inputs
		List<Type> inputTypes = new ArrayList<Type>();
		inputTypes.add(Type.arrayType());

		Type outputType = Type.arrayType();

		List<IOPair> ioPairs = new ArrayList<IOPair>();

		ioPairs.add(new IOPair(new Object[] { new int[] { 1, 2, 3 } },
				new int[] { 1, 2, 3 }));
		ioPairs.add(new IOPair(new Object[] { new int[] { 1, 2 } }, new int[] {
				1, 2 }));

		List<Object[]> repInputs = new ArrayList<Object[]>();
		repInputs.add(new Object[] { new int[] { 1, 2, 3 } });

		try {
			new DataDependenceSynthesis().doSynthesis(inputTypes, outputType, comps,
					spec, repInputs);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void strncpy() {

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

		// component
		List<Component> comps = new ArrayList<Component>();
		// comps.add(new IfElseComponent());
		// comps.add(new LtComponent());
		// comps.add(new ConstantComponent());
		// comps.add(new StringLengthComponent());
		// comps.add(new SubStringComponent());

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
		comps.add(new SubStringComponent());
		comps.add(new ConstantComponent());
		comps.add(new StringLengthComponent());

		// function inputs
		List<Type> inputTypes = new ArrayList<Type>();
		inputTypes.add(Type.arrayType());
		inputTypes.add(Type.arrayType());

		List<IOPair> ioPairs = new ArrayList<IOPair>();

		ioPairs.add(new IOPair(new Object[] { new int[] { 1, 2, 3 },
				new int[] { 4 } }, new int[] { 1, 2, 3, 4 }));

		List<Object[]> repInputs = new ArrayList<Object[]>();
		repInputs
				.add(new Object[] { new int[] { 1, 2, 3 }, new int[] { 4, 5 } });

		try {
			new DataDependenceSynthesis().doSynthesis(inputTypes, Type.arrayType(),
					comps, spec, repInputs);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}