package thirdparty.synthesis.datadependency.test;

import java.util.ArrayList;
import java.util.List;

import thirdparty.synthesis.basic.IOPair;
import thirdparty.synthesis.basic.Type;
import thirdparty.synthesis.component.Component;
import thirdparty.synthesis.component.StringLengthComponent;
import thirdparty.synthesis.datadependency.state.CSPair;
import thirdparty.synthesis.datadependency.state.DDClassSynthesis;
import thirdparty.synthesis.state.Call;
import thirdparty.synthesis.state.CallSequence;
import thirdparty.synthesis.state.ClassDescriptor;
import thirdparty.synthesis.state.StateAbstract;

public class DDClassSynthesisTest2 {

	public static void main(String[] args) {
		stringLengthTest();
	}

	public static void stringLengthTest() {

		// components

		List<Type> stateTypes = new ArrayList<Type>();
		stateTypes.add(Type.arrayType());

		StateAbstract sa = new StateAbstract(stateTypes);
		ClassDescriptor cd = new ClassDescriptor(sa);

		// methods
		List<Type> newTypes = new ArrayList<Type>();
		newTypes.add(Type.arrayType()); // initial values
		List<Component> newComps = new ArrayList<Component>();

		List<Type> lengthTypes = new ArrayList<Type>();
		List<Component> readComps = new ArrayList<Component>();
		readComps.add(new StringLengthComponent());

		cd.addMethod("new", newTypes, null, newComps);
		cd.addMethod("length", lengthTypes, Type.intType(), readComps);

		CallSequence initCs = new CallSequence();
		initCs.addCall(new Call("new", new IOPair(new Object[] { new int[] { 1,
				2 } }, null)));
		initCs.addCall(new Call("length", new IOPair(new Object[] {}, 2)));

		List<CallSequence> initCss = new ArrayList<CallSequence>();
		initCss.add(initCs);

		List<CSPair> csPairs = new ArrayList<CSPair>();
		CallSequence cs1 = new CallSequence();
		cs1.addCall(new Call("new", new IOPair(
				new Object[] { new int[] { 1, 2 } }, null)));
		cs1.addCall(new Call("length", new IOPair(new Object[] {}, 2)));
		CallSequence cs2 = new CallSequence();
		cs2.addCall(new Call("new", new IOPair(
				new Object[] { new int[] { 1 } }, null)));
		cs2.addCall(new Call("length", new IOPair(new Object[] {}, 1)));
		csPairs.add(new CSPair(cs1, cs2));

		CallSequence cs3 = new CallSequence();
		cs3.addCall(new Call("new", new IOPair(
				new Object[] { new int[] { 1, 2 } }, null)));
		cs3.addCall(new Call("length", new IOPair(new Object[] {}, 2)));
		CallSequence cs4 = new CallSequence();
		cs4.addCall(new Call("new", new IOPair(
				new Object[] { new int[] { 1, 8, 2 } }, null)));
		cs4.addCall(new Call("length", new IOPair(new Object[] {}, 3)));
		csPairs.add(new CSPair(cs3, cs4));
		
		try {
			new DDClassSynthesis().doSynthesis(cd, csPairs, initCss);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
