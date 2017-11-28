package ThirdParty.synthesis.datadependency.state;

import java.util.ArrayList;
import java.util.List;

import ThirdParty.synthesis.basic.IOPair;
import ThirdParty.synthesis.basic.Type;
import ThirdParty.synthesis.component.Component;
import ThirdParty.synthesis.component.ConstantComponent;
import ThirdParty.synthesis.component.HashGetComponent;
import ThirdParty.synthesis.component.HashPutComponent;
import ThirdParty.synthesis.component.PlusComponent;
import ThirdParty.synthesis.component.StringAssignComponent;
import ThirdParty.synthesis.component.StringLengthComponent;
import ThirdParty.synthesis.component.SubStringComponent;
import ThirdParty.synthesis.state.Call;
import ThirdParty.synthesis.state.CallSequence;
import ThirdParty.synthesis.state.ClassDescriptor;
import ThirdParty.synthesis.state.StateAbstract;

public class DDClassSynthesisTest {

	public static void main(String[] args) {
		// setGetTest();
		// addTest();
		// hashTest();
		fileTest();
	}

	public static void setGetTest() {

		// components
		List<Component> comps = new ArrayList<Component>();

		List<Type> stateTypes = new ArrayList<Type>();
		stateTypes.add(Type.intType());

		StateAbstract sa = new StateAbstract(stateTypes);
		ClassDescriptor cd = new ClassDescriptor(sa);

		// methods
		List<Type> inputTypes = new ArrayList<Type>();
		inputTypes.add(Type.intType());
		Type outputType = Type.intType();

		cd.addMethod("set", inputTypes, null, comps);
		cd.addMethod("get", new ArrayList<Type>(), outputType, comps);

		CallSequence initCs = new CallSequence();
		initCs.addCall(new Call("get", new IOPair(new int[] {}, 0)));
		initCs.addCall(new Call("set", new IOPair(new int[] { 1 }, null)));
		initCs.addCall(new Call("get", new IOPair(new int[] {}, 1)));

		List<CallSequence> initCss = new ArrayList<CallSequence>();
		initCss.add(initCs);

		List<CSPair> csPairs = new ArrayList<CSPair>();
		CallSequence cs1 = new CallSequence();
		cs1.addCall(new Call("get", new IOPair(new int[] {}, 0)));
		cs1.addCall(new Call("set", new IOPair(new int[] { 1 }, null)));
		cs1.addCall(new Call("get", new IOPair(new int[] {}, 1)));
		CallSequence cs2 = new CallSequence();
		cs2.addCall(new Call("get", new IOPair(new int[] {}, 0)));
		cs2.addCall(new Call("set", new IOPair(new int[] { 0 }, null)));
		cs2.addCall(new Call("get", new IOPair(new int[] {}, 0)));

		csPairs.add(new CSPair(cs1, cs2));

		try {
			new DDClassSynthesis().doSynthesis(cd, csPairs, initCss);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void addTest() {

		// components
		List<Component> comps = new ArrayList<Component>();
		comps.add(new PlusComponent());

		List<Type> stateTypes = new ArrayList<Type>();
		stateTypes.add(Type.intType());

		StateAbstract sa = new StateAbstract(stateTypes);
		ClassDescriptor cd = new ClassDescriptor(sa);

		// methods
		List<Type> inputTypes = new ArrayList<Type>();
		inputTypes.add(Type.intType());
		Type outputType = Type.intType();

		cd.addMethod("add", inputTypes, null, comps);
		cd.addMethod("get", new ArrayList<Type>(), outputType, comps);

		CallSequence initCs = new CallSequence();
		initCs.addCall(new Call("get", new IOPair(new int[] {}, 0)));
		initCs.addCall(new Call("add", new IOPair(new int[] { 1 }, null)));
		initCs.addCall(new Call("add", new IOPair(new int[] { 1 }, null)));
		initCs.addCall(new Call("get", new IOPair(new int[] {}, 2)));
		initCs.addCall(new Call("get", new IOPair(new int[] {}, 2)));

		List<CallSequence> initCss = new ArrayList<CallSequence>();
		initCss.add(initCs);

		List<CSPair> csPairs = new ArrayList<CSPair>();
		CallSequence cs1 = new CallSequence();
		cs1.addCall(new Call("get", new IOPair(new int[] {}, 0)));
		cs1.addCall(new Call("add", new IOPair(new int[] { 0 }, null)));
		cs1.addCall(new Call("add", new IOPair(new int[] { 1 }, null)));
		cs1.addCall(new Call("get", new IOPair(new int[] {}, 1)));
		cs1.addCall(new Call("get", new IOPair(new int[] {}, 1)));
		CallSequence cs2 = new CallSequence();
		cs2.addCall(new Call("get", new IOPair(new int[] {}, 0)));
		cs2.addCall(new Call("add", new IOPair(new int[] { 1 }, null)));
		cs2.addCall(new Call("add", new IOPair(new int[] { 1 }, null)));
		cs2.addCall(new Call("get", new IOPair(new int[] {}, 2)));
		cs2.addCall(new Call("get", new IOPair(new int[] {}, 2)));

		csPairs.add(new CSPair(cs1, cs2));

		try {
			new DDClassSynthesis().doSynthesis(cd, csPairs, initCss);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void hashTest() {

		// components
		List<Component> comps = new ArrayList<Component>();
		comps.add(new HashGetComponent());
		comps.add(new HashPutComponent());

		List<Type> stateTypes = new ArrayList<Type>();
		stateTypes.add(Type.hashType());

		StateAbstract sa = new StateAbstract(stateTypes);
		ClassDescriptor cd = new ClassDescriptor(sa);

		// methods
		List<Type> putTypes = new ArrayList<Type>();
		putTypes.add(Type.intType());
		putTypes.add(Type.arrayType());

		List<Type> getTypes = new ArrayList<Type>();
		getTypes.add(Type.intType());

		cd.addMethod("put", putTypes, null, comps);
		cd.addMethod("get", getTypes, Type.arrayType(), comps);

		CallSequence initCs = new CallSequence();
		initCs.addCall(new Call("get",
				new IOPair(new int[] { 1 }, new int[] {})));
		initCs.addCall(new Call("put", new IOPair(new Object[] { 1,
				new int[] { 1, 2, 3 } }, null)));
		initCs.addCall(new Call("get", new IOPair(new int[] { 1 }, new int[] {
				1, 2, 3 })));

		List<CallSequence> initCss = new ArrayList<CallSequence>();
		initCss.add(initCs);

		List<CSPair> csPairs = new ArrayList<CSPair>();
		CallSequence cs1 = new CallSequence();
		cs1.addCall(new Call("get", new IOPair(new int[] { 1 }, new int[] {})));
		cs1.addCall(new Call("put", new IOPair(new Object[] { 1,
				new int[] { 1, 2, 3 } }, null)));
		cs1.addCall(new Call("get", new IOPair(new int[] { 1 }, new int[] { 1,
				2, 3 })));
		CallSequence cs2 = new CallSequence();
		cs2.addCall(new Call("get", new IOPair(new int[] { 1 }, new int[] {})));
		cs2.addCall(new Call("put", new IOPair(new Object[] { 0,
				new int[] { 1, 2, 3 } }, null)));
		cs2.addCall(new Call("get", new IOPair(new int[] { 1 }, new int[] { 1,
				2, 3 })));
		csPairs.add(new CSPair(cs1, cs2));

		try {
			new DDClassSynthesis().doSynthesis(cd, csPairs, initCss);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void fileTest() {

		// components

		List<Type> stateTypes = new ArrayList<Type>();
		stateTypes.add(Type.hashType());
		stateTypes.add(Type.intType());

		StateAbstract sa = new StateAbstract(stateTypes);
		ClassDescriptor cd = new ClassDescriptor(sa);

		// methods
		List<Type> seekTypes = new ArrayList<Type>();
		seekTypes.add(Type.intType()); // fd
		seekTypes.add(Type.intType()); // offset
		List<Component> seekComps = new ArrayList<Component>();

		List<Type> readTypes = new ArrayList<Type>();
		readTypes.add(Type.intType()); // fd
		readTypes.add(Type.intType()); // count;
		List<Component> readComps = new ArrayList<Component>();
		readComps.add(new HashGetComponent());
		readComps.add(new SubStringComponent());
		readComps.add(new ConstantComponent());
		readComps.add(new StringLengthComponent());
		readComps.add(new PlusComponent());

		List<Type> writeTypes = new ArrayList<Type>();
		writeTypes.add(Type.intType()); // fd
		writeTypes.add(Type.arrayType()); // buf
		writeTypes.add(Type.intType()); // count;
		List<Component> writeComps = new ArrayList<Component>();
		writeComps.add(new HashGetComponent());
		writeComps.add(new HashPutComponent());
		writeComps.add(new StringAssignComponent());
		writeComps.add(new SubStringComponent());
		writeComps.add(new ConstantComponent());
		writeComps.add(new StringLengthComponent());
		writeComps.add(new PlusComponent());

		cd.addMethod("seek", seekTypes, null, seekComps);
		cd.addMethod("read", readTypes, Type.arrayType(), readComps);
		cd.addMethod("write", writeTypes, null, writeComps);

		CallSequence initCs = new CallSequence();
		initCs.addCall(new Call("write", new IOPair(new Object[] { 1,
				new int[] { 1, 2 }, 2 }, null)));
		initCs.addCall(new Call("write", new IOPair(new Object[] { 1,
				new int[] { 3, 4 }, 2 }, null)));
		initCs.addCall(new Call("seek", new IOPair(new int[] { 1, 0 }, null)));
		initCs.addCall(new Call("read", new IOPair(new int[] { 1, 3 },
				new int[] { 1, 2, 3 })));

		List<CallSequence> initCss = new ArrayList<CallSequence>();
		initCss.add(initCs);

		List<CSPair> csPairs = new ArrayList<CSPair>();
		CallSequence cs1 = new CallSequence();
		cs1.addCall(new Call("write", new IOPair(new Object[] { 1,
				new int[] { 1, 2 }, 2 }, null)));
		cs1.addCall(new Call("write", new IOPair(new Object[] { 1,
				new int[] { 3, 4 }, 2 }, null)));
		cs1.addCall(new Call("seek", new IOPair(new int[] { 1, 0 }, null)));
		cs1.addCall(new Call("read", new IOPair(new int[] { 1, 3 }, new int[] {
				1, 2, 3 })));
		CallSequence cs2 = new CallSequence();
		cs2.addCall(new Call("write", new IOPair(new Object[] { 1,
				new int[] { 1, 2 }, 2 }, null)));
		cs2.addCall(new Call("write", new IOPair(new Object[] { 1,
				new int[] { 4 }, 2 }, null)));
		cs2.addCall(new Call("seek", new IOPair(new int[] { 1, 0 }, null)));
		cs2.addCall(new Call("read", new IOPair(new int[] { 1, 3 }, new int[] {
				1, 2, 4 })));
		csPairs.add(new CSPair(cs1, cs2));

		CallSequence cs3 = new CallSequence();
		cs3.addCall(new Call("write", new IOPair(new Object[] { 1,
				new int[] { 1, 2 }, 2 }, null)));
		cs3.addCall(new Call("write", new IOPair(new Object[] { 1,
				new int[] { 3, 4 }, 2 }, null)));
		cs3.addCall(new Call("seek", new IOPair(new int[] { 1, 0 }, null)));
		cs3.addCall(new Call("read", new IOPair(new int[] { 1, 3 }, new int[] {
				1, 2, 3 })));
		CallSequence cs4 = new CallSequence();
		cs4.addCall(new Call("write", new IOPair(new Object[] { 1,
				new int[] { 2 }, 2 }, null)));
		cs4.addCall(new Call("write", new IOPair(new Object[] { 1,
				new int[] { 3, 4 }, 2 }, null)));
		cs4.addCall(new Call("seek", new IOPair(new int[] { 1, 0 }, null)));
		cs4.addCall(new Call("read", new IOPair(new int[] { 1, 3 }, new int[] {
				2, 3, 4 })));
		// csPairs.add(new CSPair(cs3, cs4));
		
		CallSequence cs5 = new CallSequence();
		cs5.addCall(new Call("write", new IOPair(new Object[] { 1,
				new int[] { 1, 2 }, 2 }, null)));
		cs5.addCall(new Call("write", new IOPair(new Object[] { 1,
				new int[] { 3, 4 }, 2 }, null)));
		cs5.addCall(new Call("seek", new IOPair(new int[] { 1, 0 }, null)));
		cs5.addCall(new Call("read", new IOPair(new int[] { 1, 3 }, new int[] {
				1, 2, 3 })));
		CallSequence cs6 = new CallSequence();
		cs6.addCall(new Call("write", new IOPair(new Object[] { 1,
				new int[] { 1 }, 2 }, null)));
		cs6.addCall(new Call("write", new IOPair(new Object[] { 1,
				new int[] { 3, 4 }, 2 }, null)));
		cs6.addCall(new Call("seek", new IOPair(new int[] { 1, 0 }, null)));
		cs6.addCall(new Call("read", new IOPair(new int[] { 1, 3 }, new int[] {
				1, 3, 4 })));
		// csPairs.add(new CSPair(cs5, cs6));

		try {
			new DDClassSynthesis().doSynthesis(cd, csPairs, initCss);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
