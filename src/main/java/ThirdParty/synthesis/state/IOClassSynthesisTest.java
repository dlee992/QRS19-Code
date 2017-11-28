package ThirdParty.synthesis.state;

import java.util.ArrayList;
import java.util.List;

import ThirdParty.synthesis.basic.IOPair;
import ThirdParty.synthesis.basic.Type;
import ThirdParty.synthesis.component.Component;
import ThirdParty.synthesis.component.ConstantComponent;
import ThirdParty.synthesis.component.HashGetComponent;
import ThirdParty.synthesis.component.HashPutComponent;
import ThirdParty.synthesis.component.MinusComponent;
import ThirdParty.synthesis.component.PlusComponent;
import ThirdParty.synthesis.component.StringAddComponent;
import ThirdParty.synthesis.component.StringAssignComponent;
import ThirdParty.synthesis.component.StringIndexAtComponent;
import ThirdParty.synthesis.component.StringIndexOfCharComponent;
import ThirdParty.synthesis.component.StringLengthComponent;
import ThirdParty.synthesis.component.StringRemoveComponent;
import ThirdParty.synthesis.component.SubStringComponent;

public class IOClassSynthesisTest {

	public static void main(String[] args) {
		// setGetTest();
		// addTest();
		// hashTest();
		// fileTest();
		// stackTest();
		// queueTest();
		// listTest();
		// listTest2();
		// listTest21();
		listTest22();
		// listTest3();
		// listTest4();
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

		CallSequence cs1 = new CallSequence();
		cs1.addCall(new Call("get", new IOPair(new int[] {}, 0)));
		cs1.addCall(new Call("set", new IOPair(new int[] { 1 }, null)));
		cs1.addCall(new Call("get", new IOPair(new int[] {}, 1)));

		List<CallSequence> css = new ArrayList<CallSequence>();
		css.add(cs1);

		try {
			new IOClassSynthesis().doSynthesis(cd, css);
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

		CallSequence cs1 = new CallSequence();
		cs1.addCall(new Call("get", new IOPair(new int[] {}, 0)));
		cs1.addCall(new Call("add", new IOPair(new int[] { 1 }, null)));
		cs1.addCall(new Call("add", new IOPair(new int[] { 1 }, null)));
		cs1.addCall(new Call("get", new IOPair(new int[] {}, 2)));
		cs1.addCall(new Call("get", new IOPair(new int[] {}, 2)));

		CallSequence cs2 = new CallSequence();
		cs2.addCall(new Call("get", new IOPair(new int[] {}, 0)));
		cs2.addCall(new Call("add", new IOPair(new int[] { 0 }, null)));
		cs2.addCall(new Call("add", new IOPair(new int[] { 1 }, null)));
		cs2.addCall(new Call("get", new IOPair(new int[] {}, 1)));
		cs2.addCall(new Call("get", new IOPair(new int[] {}, 1)));

		List<CallSequence> css = new ArrayList<CallSequence>();
		css.add(cs1);
		css.add(cs2);

		try {
			new IOClassSynthesis().doSynthesis(cd, css);
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

		CallSequence cs1 = new CallSequence();
		cs1.addCall(new Call("get", new IOPair(new int[] { 1 }, new int[] {})));
		cs1.addCall(new Call("put", new IOPair(new Object[] { 1,
				new int[] { 1, 2, 3 } }, null)));
		cs1.addCall(new Call("get", new IOPair(new int[] { 1 }, new int[] { 1,
				2, 3 })));

		List<CallSequence> css = new ArrayList<CallSequence>();
		css.add(cs1);

		try {
			new IOClassSynthesis().doSynthesis(cd, css);
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
		writeComps.add(new PlusComponent());
		writeComps.add(new ConstantComponent());

		cd.addMethod("seek", seekTypes, null, seekComps);
		cd.addMethod("read", readTypes, Type.arrayType(), readComps);
		cd.addMethod("write", writeTypes, null, writeComps);

		CallSequence cs1 = new CallSequence();
		cs1.addCall(new Call("write", new IOPair(new Object[] { 1,
				new int[] { 1, 2, 3, 4, 5 }, 5 }, null)));
		cs1.addCall(new Call("write", new IOPair(new Object[] { 1,
				new int[] { 6, 7, 8 }, 3 }, null)));
		cs1.addCall(new Call("write", new IOPair(new Object[] { 1,
				new int[] { 9, 8, 9 }, 1 }, null)));
		cs1.addCall(new Call("seek", new IOPair(new int[] { 1, 0 }, null)));
		cs1.addCall(new Call("read", new IOPair(new int[] { 1, 4 }, new int[] {
				1, 2, 3, 4 })));
		// cs1.addCall(new Call("read", new IOPair(new int[] { 1, 5 }, new int[]
		// {
		// 5, 6, 7, 8, 9 })));

		List<CallSequence> css = new ArrayList<CallSequence>();
		css.add(cs1);

		try {
			new IOClassSynthesis().doSynthesis(cd, css);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void stackTest() {

		// components
		List<Type> stateTypes = new ArrayList<Type>();
		stateTypes.add(Type.arrayType());

		StateAbstract sa = new StateAbstract(stateTypes);
		ClassDescriptor cd = new ClassDescriptor(sa);

		// methods
		List<Type> sizeTypes = new ArrayList<Type>();
		List<Component> sizeComps = new ArrayList<Component>();
		sizeComps.add(new StringLengthComponent());

		List<Type> pushTypes = new ArrayList<Type>();
		pushTypes.add(Type.intType()); // value
		List<Component> pushComps = new ArrayList<Component>();
		pushComps.add(new StringAddComponent());
		pushComps.add(new StringLengthComponent());

		List<Type> popTypes = new ArrayList<Type>();
		List<Component> popComps = new ArrayList<Component>();
		popComps.add(new StringLengthComponent());
		popComps.add(new ConstantComponent());
		popComps.add(new MinusComponent());
		popComps.add(new StringIndexAtComponent());
		popComps.add(new StringRemoveComponent());

		cd.addMethod("size", sizeTypes, Type.intType(), sizeComps);
		cd.addMethod("push", pushTypes, Type.intType(), pushComps);
		cd.addMethod("pop", popTypes, Type.intType(), popComps);

		CallSequence cs1 = new CallSequence();
		cs1.addCall(new Call("size", new IOPair(new Object[] {}, 0)));
		cs1.addCall(new Call("push", new IOPair(new Object[] { 2 }, 2)));
		cs1.addCall(new Call("push", new IOPair(new Object[] { 3 }, 3)));
		cs1.addCall(new Call("pop", new IOPair(new int[] {}, 3)));
		cs1.addCall(new Call("pop", new IOPair(new int[] {}, 2)));

		CallSequence cs2 = new CallSequence();
		cs2.addCall(new Call("size", new IOPair(new Object[] {}, 0)));
		cs2.addCall(new Call("push", new IOPair(new Object[] { 5 }, 5)));
		cs2.addCall(new Call("pop", new IOPair(new int[] {}, 5)));

		List<CallSequence> css = new ArrayList<CallSequence>();
		css.add(cs1);
		// css.add(cs2);

		try {
			new IOClassSynthesis().doSynthesis(cd, css);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void queueTest() {

		// components
		List<Type> stateTypes = new ArrayList<Type>();
		stateTypes.add(Type.arrayType());

		StateAbstract sa = new StateAbstract(stateTypes);
		ClassDescriptor cd = new ClassDescriptor(sa);

		// methods
		List<Type> sizeTypes = new ArrayList<Type>();
		List<Component> sizeComps = new ArrayList<Component>();
		sizeComps.add(new StringLengthComponent());

		List<Type> addTypes = new ArrayList<Type>();
		addTypes.add(Type.intType()); // value
		List<Component> addComps = new ArrayList<Component>();
		addComps.add(new StringAddComponent());
		addComps.add(new StringLengthComponent());

		List<Type> offerTypes = new ArrayList<Type>();
		offerTypes.add(Type.intType()); // value
		List<Component> offerComps = new ArrayList<Component>();
		offerComps.add(new StringAddComponent());
		offerComps.add(new StringLengthComponent());

		List<Type> peekTypes = new ArrayList<Type>();
		List<Component> peekComps = new ArrayList<Component>();
		peekComps.add(new ConstantComponent());
		peekComps.add(new StringIndexAtComponent());

		List<Type> pollTypes = new ArrayList<Type>();
		List<Component> pollComps = new ArrayList<Component>();
		pollComps.add(new ConstantComponent());
		pollComps.add(new StringIndexAtComponent());
		pollComps.add(new StringRemoveComponent());

		cd.addMethod("size", sizeTypes, Type.intType(), sizeComps);
		cd.addMethod("add", addTypes, Type.intType(), addComps);
		cd.addMethod("offer", offerTypes, Type.intType(), offerComps);
		cd.addMethod("peek", peekTypes, Type.intType(), peekComps);
		cd.addMethod("poll", pollTypes, Type.intType(), pollComps);

		CallSequence cs1 = new CallSequence();
		cs1.addCall(new Call("size", new IOPair(new Object[] {}, 0)));
		cs1.addCall(new Call("add", new IOPair(new Object[] { 2 }, 2)));
		cs1.addCall(new Call("offer", new IOPair(new Object[] { 3 }, 3)));
		cs1.addCall(new Call("peek", new IOPair(new int[] {}, 2)));
		cs1.addCall(new Call("poll", new IOPair(new int[] {}, 2)));
		cs1.addCall(new Call("poll", new IOPair(new int[] {}, 3)));

		List<CallSequence> css = new ArrayList<CallSequence>();
		css.add(cs1);

		try {
			new IOClassSynthesis().doSynthesis(cd, css);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void listTest() {

		// components
		List<Type> stateTypes = new ArrayList<Type>();
		stateTypes.add(Type.arrayType());

		StateAbstract sa = new StateAbstract(stateTypes);
		ClassDescriptor cd = new ClassDescriptor(sa);

		// methods
		List<Type> sizeTypes = new ArrayList<Type>();
		List<Component> sizeComps = new ArrayList<Component>();
		sizeComps.add(new StringLengthComponent());

		List<Type> add1Types = new ArrayList<Type>();
		add1Types.add(Type.intType()); // value
		List<Component> add1Comps = new ArrayList<Component>();
		add1Comps.add(new StringAddComponent());
		add1Comps.add(new StringLengthComponent());

		List<Type> add2Types = new ArrayList<Type>();
		add2Types.add(Type.intType()); // index
		add2Types.add(Type.intType()); // value
		List<Component> add2Comps = new ArrayList<Component>();
		add2Comps.add(new StringAddComponent());

		List<Type> remove1Types = new ArrayList<Type>();
		List<Component> remove1Comps = new ArrayList<Component>();
		remove1Comps.add(new StringLengthComponent());
		remove1Comps.add(new ConstantComponent());
		remove1Comps.add(new MinusComponent());
		remove1Comps.add(new StringIndexAtComponent());
		remove1Comps.add(new StringRemoveComponent());

		List<Type> remove2Types = new ArrayList<Type>();
		remove2Types.add(Type.intType());
		List<Component> remove2Comps = new ArrayList<Component>();
		remove2Comps.add(new StringIndexAtComponent());
		remove2Comps.add(new StringRemoveComponent());

		cd.addMethod("size", sizeTypes, Type.intType(), sizeComps);
		cd.addMethod("add1", add1Types, Type.intType(), add1Comps);
		cd.addMethod("add2", add2Types, Type.intType(), add2Comps);
		cd.addMethod("remove1", remove1Types, Type.intType(), remove1Comps);
		cd.addMethod("remove2", remove2Types, Type.intType(), remove2Comps);

		CallSequence cs1 = new CallSequence();
		cs1.addCall(new Call("size", new IOPair(new Object[] {}, 0)));
		cs1.addCall(new Call("add1", new IOPair(new Object[] { 2 }, 2)));
		cs1.addCall(new Call("add2", new IOPair(new Object[] { 0, 3 }, 3)));
		cs1.addCall(new Call("remove1", new IOPair(new int[] {}, 2)));
		cs1.addCall(new Call("remove2", new IOPair(new int[] { 0 }, 3)));

		CallSequence cs2 = new CallSequence();
		cs2.addCall(new Call("size", new IOPair(new Object[] {}, 0)));
		cs2.addCall(new Call("add1", new IOPair(new Object[] { 8 }, 8)));
		cs2.addCall(new Call("add2", new IOPair(new Object[] { 1, 9 }, 9)));
		cs2.addCall(new Call("remove1", new IOPair(new int[] {}, 9)));
		cs2.addCall(new Call("remove2", new IOPair(new int[] { 0 }, 8)));

		CallSequence cs3 = new CallSequence();
		cs3.addCall(new Call("size", new IOPair(new Object[] {}, 0)));
		cs3.addCall(new Call("add1", new IOPair(new Object[] { 8 }, 8)));
		cs3.addCall(new Call("add2", new IOPair(new Object[] { 1, 9 }, 9)));
		cs3.addCall(new Call("remove1", new IOPair(new int[] {}, 9)));
		cs3.addCall(new Call("remove1", new IOPair(new int[] {}, 8)));

		CallSequence cs4 = new CallSequence();
		cs4.addCall(new Call("size", new IOPair(new Object[] {}, 0)));
		cs4.addCall(new Call("add1", new IOPair(new Object[] { 8 }, 8)));
		cs4.addCall(new Call("add2", new IOPair(new Object[] { 1, 9 }, 9)));
		cs4.addCall(new Call("remove2", new IOPair(new int[] { 0 }, 8)));
		cs4.addCall(new Call("remove2", new IOPair(new int[] { 0 }, 9)));

		List<CallSequence> css = new ArrayList<CallSequence>();
		css.add(cs1);
		css.add(cs2);
		css.add(cs3);
		css.add(cs4);

		try {
			new IOClassSynthesis().doSynthesis(cd, css);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void listTest2() {

		// components
		List<Type> stateTypes = new ArrayList<Type>();
		stateTypes.add(Type.arrayType());

		StateAbstract sa = new StateAbstract(stateTypes);
		ClassDescriptor cd = new ClassDescriptor(sa);

		// methods
		List<Type> sizeTypes = new ArrayList<Type>();
		List<Component> sizeComps = new ArrayList<Component>();
		sizeComps.add(new StringLengthComponent());

		List<Type> add1Types = new ArrayList<Type>();
		add1Types.add(Type.intType()); // value
		List<Component> add1Comps = new ArrayList<Component>();
		add1Comps.add(new StringAddComponent());
		add1Comps.add(new StringLengthComponent());

		List<Type> remove1Types = new ArrayList<Type>();
		List<Component> removeComps = new ArrayList<Component>();
		removeComps.add(new StringLengthComponent());
		removeComps.add(new ConstantComponent());
		removeComps.add(new MinusComponent());
		removeComps.add(new StringIndexAtComponent());
		removeComps.add(new StringRemoveComponent());

		cd.addMethod("size", sizeTypes, Type.intType(), sizeComps);
		cd.addMethod("add", add1Types, Type.intType(), add1Comps);
		cd.addMethod("remove", remove1Types, Type.intType(), removeComps);

		CallSequence cs1 = new CallSequence();
		cs1.addCall(new Call("size", new IOPair(new Object[] {}, 0)));
		cs1.addCall(new Call("add", new IOPair(new Object[] { 2 }, 2)));
		cs1.addCall(new Call("remove", new IOPair(new Object[] { 2 }, 2)));

		CallSequence cs2 = new CallSequence();
		cs2.addCall(new Call("add", new IOPair(new Object[] { 8 }, 8)));
		cs2.addCall(new Call("size", new IOPair(new Object[] {}, 1)));
		cs2.addCall(new Call("add", new IOPair(new Object[] { 7 }, 7)));
		cs2.addCall(new Call("remove", new IOPair(new int[] {}, 7)));
		cs2.addCall(new Call("size", new IOPair(new int[] {}, 1)));

		CallSequence cs3 = new CallSequence();
		cs3.addCall(new Call("add", new IOPair(new Object[] { 2 }, 2)));
		cs3.addCall(new Call("add", new IOPair(new Object[] { 9 }, 9)));
		cs3.addCall(new Call("size", new IOPair(new Object[] {}, 2)));
		cs3.addCall(new Call("size", new IOPair(new int[] {}, 2)));
		cs3.addCall(new Call("remove", new IOPair(new int[] {}, 9)));

		CallSequence cs4 = new CallSequence();
		cs4.addCall(new Call("add", new IOPair(new Object[] { 5 }, 5)));
		cs4.addCall(new Call("size", new IOPair(new Object[] {}, 1)));
		cs4.addCall(new Call("size", new IOPair(new Object[] {}, 1)));
		cs4.addCall(new Call("add", new IOPair(new int[] { 1 }, 1)));
		cs4.addCall(new Call("remove", new IOPair(new int[] {}, 1)));

		CallSequence cs5 = new CallSequence();
		cs5.addCall(new Call("add", new IOPair(new Object[] { 1 }, 1)));
		cs5.addCall(new Call("add", new IOPair(new Object[] { 4 }, 4)));
		cs5.addCall(new Call("size", new IOPair(new Object[] {}, 2)));
		cs5.addCall(new Call("remove", new IOPair(new int[] {}, 4)));
		cs5.addCall(new Call("size", new IOPair(new int[] {}, 1)));

		CallSequence cs6 = new CallSequence();
		cs6.addCall(new Call("add", new IOPair(new Object[] { 8 }, 8)));
		cs6.addCall(new Call("add", new IOPair(new Object[] { 3 }, 3)));
		cs6.addCall(new Call("remove", new IOPair(new Object[] {}, 3)));
		cs6.addCall(new Call("add", new IOPair(new int[] { 5 }, 5)));
		cs6.addCall(new Call("size", new IOPair(new int[] {}, 2)));

		List<CallSequence> css = new ArrayList<CallSequence>();
		css.add(cs1);
		css.add(cs2);
		css.add(cs3);
		css.add(cs4);
		css.add(cs5);
		css.add(cs6);

		try {
			new IOClassSynthesis().doSynthesis(cd, css);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void listTest21() {

		// components
		List<Type> stateTypes = new ArrayList<Type>();
		stateTypes.add(Type.arrayType());

		StateAbstract sa = new StateAbstract(stateTypes);
		ClassDescriptor cd = new ClassDescriptor(sa);

		// methods
		List<Type> sizeTypes = new ArrayList<Type>();
		List<Component> sizeComps = new ArrayList<Component>();
		sizeComps.add(new StringLengthComponent());

		List<Type> add1Types = new ArrayList<Type>();
		add1Types.add(Type.intType()); // value
		List<Component> add1Comps = new ArrayList<Component>();
		add1Comps.add(new StringAddComponent());
		add1Comps.add(new StringLengthComponent());

		List<Type> getTypes = new ArrayList<Type>();
		getTypes.add(Type.intType()); // index
		List<Component> getComps = new ArrayList<Component>();
		getComps.add(new StringIndexAtComponent());
		getComps.add(new StringLengthComponent());

		List<Type> remove1Types = new ArrayList<Type>();
		List<Component> removeComps = new ArrayList<Component>();
		removeComps.add(new StringLengthComponent());
		removeComps.add(new ConstantComponent());
		removeComps.add(new MinusComponent());
		removeComps.add(new StringIndexAtComponent());
		removeComps.add(new StringRemoveComponent());

		cd.addMethod("size", sizeTypes, Type.intType(), sizeComps);
		cd.addMethod("add", add1Types, Type.intType(), add1Comps);
		cd.addMethod("remove", remove1Types, Type.intType(), removeComps);
		// cd.addMethod("get", getTypes, Type.intType(), getComps);

		CallSequence cs1 = new CallSequence();
		cs1.addCall(new Call("size", new IOPair(new Object[] {}, 0)));
		cs1.addCall(new Call("add", new IOPair(new Object[] { 2 }, 2)));
		cs1.addCall(new Call("remove", new IOPair(new Object[] { 2 }, 2)));

		CallSequence cs2 = new CallSequence();
		cs2.addCall(new Call("add", new IOPair(new Object[] { 1 }, 1)));
		cs2.addCall(new Call("add", new IOPair(new Object[] { 7 }, 7)));
		cs2.addCall(new Call("size", new IOPair(new Object[] {}, 2)));
		cs2.addCall(new Call("size", new IOPair(new int[] {}, 2)));
		cs2.addCall(new Call("remove", new IOPair(new int[] {}, 7)));

		CallSequence cs3 = new CallSequence();
		cs3.addCall(new Call("add", new IOPair(new Object[] { 9 }, 9)));
		cs3.addCall(new Call("remove", new IOPair(new Object[] {}, 9)));
		cs3.addCall(new Call("size", new IOPair(new Object[] {}, 0)));
		cs3.addCall(new Call("size", new IOPair(new int[] {}, 0)));
		cs3.addCall(new Call("size", new IOPair(new int[] {}, 0)));

		CallSequence cs4 = new CallSequence();
		cs4.addCall(new Call("add", new IOPair(new Object[] { 2 }, 2)));
		cs4.addCall(new Call("add", new IOPair(new Object[] { 8 }, 8)));
		cs4.addCall(new Call("remove", new IOPair(new Object[] {}, 8)));
		cs4.addCall(new Call("size", new IOPair(new int[] {}, 1)));
		cs4.addCall(new Call("remove", new IOPair(new int[] {}, 2)));

		CallSequence cs5 = new CallSequence();
		cs5.addCall(new Call("add", new IOPair(new Object[] { 1 }, 1)));
		cs5.addCall(new Call("add", new IOPair(new Object[] { 4 }, 4)));
		cs5.addCall(new Call("size", new IOPair(new Object[] {}, 2)));
		cs5.addCall(new Call("remove", new IOPair(new int[] {}, 4)));
		cs5.addCall(new Call("size", new IOPair(new int[] {}, 1)));

		CallSequence cs6 = new CallSequence();
		cs6.addCall(new Call("add", new IOPair(new Object[] { 8 }, 8)));
		cs6.addCall(new Call("add", new IOPair(new Object[] { 3 }, 3)));
		cs6.addCall(new Call("remove", new IOPair(new Object[] {}, 3)));
		cs6.addCall(new Call("add", new IOPair(new int[] { 5 }, 5)));
		cs6.addCall(new Call("size", new IOPair(new int[] {}, 2)));

		List<CallSequence> css = new ArrayList<CallSequence>();
		css.add(cs1);
		css.add(cs2);
		css.add(cs3);
		css.add(cs4);
		// css.add(cs5);
		// css.add(cs6);

		try {
			new IOClassSynthesis().doSynthesis(cd, css);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void listTest22() {

		// components
		List<Type> stateTypes = new ArrayList<Type>();
		stateTypes.add(Type.arrayType());

		StateAbstract sa = new StateAbstract(stateTypes);
		ClassDescriptor cd = new ClassDescriptor(sa);

		// methods
		List<Type> sizeTypes = new ArrayList<Type>();
		List<Component> sizeComps = new ArrayList<Component>();
		sizeComps.add(new StringLengthComponent());

		List<Type> addTypes = new ArrayList<Type>();
		addTypes.add(Type.intType()); // value
		List<Component> addComps = new ArrayList<Component>();
		addComps.add(new StringAddComponent());
		addComps.add(new StringLengthComponent());
		addComps.add(new ConstantComponent());

		List<Type> getTypes = new ArrayList<Type>();
		getTypes.add(Type.intType()); // index
		List<Component> getComps = new ArrayList<Component>();
		getComps.add(new StringIndexAtComponent());
		getComps.add(new StringLengthComponent());

		cd.addMethod("size", sizeTypes, Type.intType(), sizeComps);
		cd.addMethod("add", addTypes, Type.intType(), addComps);
		cd.addMethod("get", getTypes, Type.intType(), getComps);

		CallSequence cs1 = new CallSequence();
		cs1.addCall(new Call("size", new IOPair(new Object[] {}, 0)));
		cs1.addCall(new Call("add", new IOPair(new Object[] { 2 }, 2)));

		CallSequence cs2 = new CallSequence();
		cs2.addCall(new Call("size", new IOPair(new Object[] {}, 0)));
		cs2.addCall(new Call("add", new IOPair(new Object[] { 7 }, 7)));
		cs2.addCall(new Call("size", new IOPair(new Object[] {}, 1)));

		CallSequence cs3 = new CallSequence();
		cs3.addCall(new Call("size", new IOPair(new Object[] {}, 0)));
		cs3.addCall(new Call("add", new IOPair(new Object[] { 7 }, 7)));
		cs3.addCall(new Call("get", new IOPair(new int[] { 0 }, 7)));

		CallSequence cs4 = new CallSequence();
		cs4.addCall(new Call("add", new IOPair(new Object[] { 8 }, 8)));
		cs4.addCall(new Call("get", new IOPair(new Object[] { 0 }, 8)));
		cs4.addCall(new Call("size", new IOPair(new int[] {}, 1)));

		List<CallSequence> css = new ArrayList<CallSequence>();
		css.add(cs1);
		css.add(cs2);
		css.add(cs3);
		css.add(cs4);
		// css.add(cs5);
		// css.add(cs6);

		try {
			new IOClassSynthesis().doSynthesis(cd, css);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void listTest3() {

		// components
		List<Type> stateTypes = new ArrayList<Type>();
		stateTypes.add(Type.arrayType());

		StateAbstract sa = new StateAbstract(stateTypes);
		ClassDescriptor cd = new ClassDescriptor(sa);

		// methods
		List<Type> sizeTypes = new ArrayList<Type>();
		List<Component> sizeComps = new ArrayList<Component>();
		sizeComps.add(new StringLengthComponent());

		List<Type> addTypes = new ArrayList<Type>();
		addTypes.add(Type.intType()); // value
		List<Component> addComps = new ArrayList<Component>();
		addComps.add(new StringAddComponent());
		addComps.add(new StringLengthComponent());

		List<Type> removeTypes = new ArrayList<Type>();
		removeTypes.add(Type.intType());
		List<Component> removeComps = new ArrayList<Component>();
		removeComps.add(new StringIndexOfCharComponent());
		removeComps.add(new StringIndexAtComponent());
		removeComps.add(new StringRemoveComponent());

		cd.addMethod("size", sizeTypes, Type.intType(), sizeComps);
		cd.addMethod("add", addTypes, Type.intType(), addComps);
		cd.addMethod("remove", removeTypes, Type.intType(), removeComps);

		CallSequence cs1 = new CallSequence();
		cs1.addCall(new Call("size", new IOPair(new Object[] {}, 0)));
		cs1.addCall(new Call("add", new IOPair(new Object[] { 2 }, 2)));
		cs1.addCall(new Call("add", new IOPair(new Object[] { 3 }, 3)));
		cs1.addCall(new Call("remove", new IOPair(new int[] { 2 }, 2)));
		cs1.addCall(new Call("remove", new IOPair(new int[] { 3 }, 3)));
		cs1.addCall(new Call("size", new IOPair(new Object[] {}, 0)));

		List<CallSequence> css = new ArrayList<CallSequence>();
		css.add(cs1);

		try {
			new IOClassSynthesis().doSynthesis(cd, css);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void listTest4() {

		// components
		List<Type> stateTypes = new ArrayList<Type>();
		stateTypes.add(Type.arrayType());

		StateAbstract sa = new StateAbstract(stateTypes);
		ClassDescriptor cd = new ClassDescriptor(sa);

		// methods
		List<Type> sizeTypes = new ArrayList<Type>();
		List<Component> sizeComps = new ArrayList<Component>();
		sizeComps.add(new StringLengthComponent());

		List<Type> addTypes = new ArrayList<Type>();
		addTypes.add(Type.intType()); // value
		List<Component> addComps = new ArrayList<Component>();
		addComps.add(new StringAddComponent());
		addComps.add(new StringLengthComponent());

		List<Type> removeTypes = new ArrayList<Type>();
		removeTypes.add(Type.intType());
		List<Component> removeComps = new ArrayList<Component>();
		removeComps.add(new StringIndexOfCharComponent());
		removeComps.add(new StringIndexAtComponent());
		removeComps.add(new StringRemoveComponent());

		cd.addMethod("size", sizeTypes, Type.intType(), sizeComps);
		cd.addMethod("add", addTypes, Type.intType(), addComps);
		cd.addMethod("remove", removeTypes, Type.intType(), removeComps);

		CallSequence cs1 = new CallSequence();
		cs1.addCall(new Call("size", new IOPair(new Object[] {}, 0)));
		cs1.addCall(new Call("add", new IOPair(new Object[] { 2 }, 2)));
		cs1.addCall(new Call("remove", new IOPair(new Object[] { 2 }, 2)));

		CallSequence cs2 = new CallSequence();
		cs2.addCall(new Call("size", new IOPair(new Object[] {}, 0)));
		cs2.addCall(new Call("add", new IOPair(new Object[] { 8 }, 8)));
		cs2.addCall(new Call("remove", new IOPair(new Object[] { 8 }, 8)));
		cs2.addCall(new Call("add", new IOPair(new Object[] { 4 }, 4)));
		cs2.addCall(new Call("size", new IOPair(new Object[] {}, 1)));

		List<CallSequence> css = new ArrayList<CallSequence>();
		css.add(cs1);
		css.add(cs2);

		try {
			new IOClassSynthesis().doSynthesis(cd, css);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
