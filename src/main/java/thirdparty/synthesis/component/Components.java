package thirdparty.synthesis.component;

import com.microsoft.z3.Context;

import java.util.ArrayList;
import java.util.List;

public class Components {
	public static int index = 1;
	public final static int PLUS = index++;
	public final static int MINUS = index++;
	public final static int MULT = index++;
	public final static int DIV = index++;
	public final static int CONSTANT = index++;
	public final static int SUM = index++;
	public final static int AVERAGE = index++;
	public final static int MAX = index++;
	public final static int MIN = index++;
	public final static int IFELSE = index++;
	public final static int EQUAL = index++;
	public final static int NOT = index++;
	public final static int GT = index++;
	public final static int GE = index++;
	public final static int LT = index++;
	public final static int LE = index++;
	public final static int AND = index++;
	public final static int OR = index++;
	public final static int ABS = index++;
	public final static int SUBSTRING = index++;
	public final static int STRINGASSIGN = index++;
	public final static int STRINGLENGTH = index++;
	public final static int STRINGCONCAT = index++;
	public final static int STRINGINDEXAT = index++;
	public final static int STRINGCONTAIN = index++;
	public final static int STRINGINDEXOFSTRING = index++;
	public final static int STRINGINDEXOFCHAR = index++;
	public final static int STRINGADD = index++;
	public final static int STRINGREMOVE = index++;
	public final static int STRINGN21 = index++;
	public final static int STRINGN2N = index++;
	public final static int HASHGET = index++;
	public final static int HASHPUT = index++;

	public static List<Component> getComponents(List<Component> components,
			Context ctx) {
		List<Component> newComps = new ArrayList<Component>();
		for (Component comp : components) {
			Component c = null;
			if (comp.type == PLUS) {
				c = new PlusComponent(comp.varType);
			} else if (comp.type == MINUS) {
				c = new MinusComponent(comp.varType);
			} else if (comp.type == MULT) {
				c = new MultComponent(comp.varType);
			} else if (comp.type == DIV) {
				c = new DivComponent(comp.varType);
			} else if (comp.type == CONSTANT) {
				c = new ConstantComponent(comp.varType,
						((ConstantComponent) comp).defaultValue);
			} else if (comp.type == SUM) {
				c = new SumComponent(((SumComponent) comp).getInputNum(),
						comp.varType);
			} else if (comp.type == AVERAGE) {
				c = new AverageComponent(((AverageComponent) comp).getInputNum(),
						comp.varType);
			} else if (comp.type == MAX) {
				c = new MaxComponent(((MaxComponent) comp).getInputNum(),
						comp.varType);
			} else if (comp.type == MIN) {
				c = new MinComponent(((MinComponent) comp).getInputNum(),
						comp.varType);
			} else if (comp.type == IFELSE) {
				c = new IfElseComponent(comp.varType);
			} else if (comp.type == EQUAL) {
				c = new EqualComponent(comp.varType);
			} else if (comp.type == NOT) {
				c = new NotComponent();
			} else if (comp.type == GT) {
				c = new GTComponent(comp.varType);
			} else if (comp.type == GE) {
				c = new GEComponent(comp.varType);
			} else if (comp.type == LT) {
				c = new LtComponent(comp.varType);
			} else if (comp.type == LE) {
				c = new LEComponent(comp.varType);
			} else if (comp.type == AND) {
				c = new AndComponent();
			} else if (comp.type == OR) {
				c = new OrComponent();
			} else if (comp.type == ABS) {
				c = new AbsComponent(comp.varType);
			} else if (comp.type == SUBSTRING) {
				c = new SubStringComponent();
			} else if (comp.type == STRINGASSIGN) {
				c = new StringAssignComponent();
			} else if (comp.type == STRINGLENGTH) {
				c = new StringLengthComponent();
			} else if (comp.type == STRINGCONCAT) {
				c = new StringConcatComponent();
			} else if (comp.type == STRINGINDEXAT) {
				c = new StringIndexAtComponent();
			} else if (comp.type == STRINGCONTAIN) {
				c = new StringContainComponent();
			} else if (comp.type == STRINGINDEXOFSTRING) {
				c = new StringIndexOfStringComponent();
			} else if (comp.type == STRINGINDEXOFCHAR) {
				c = new StringIndexOfCharComponent();
			} else if (comp.type == STRINGADD) {
				c = new StringAddComponent();
			} else if (comp.type == STRINGREMOVE) {
				c = new StringRemoveComponent();
			} else if (comp.type == STRINGN21) {
				c = new StringN21Component();
			} else if (comp.type == STRINGN2N) {
				c = new StringN2NComponent();
			} else if (comp.type == HASHGET) {
				c = new HashGetComponent();
			} else if (comp.type == HASHPUT) {
				c = new HashPutComponent();
			}
			try {
				c.init(ctx);
			} catch (Exception e) {
				System.err.print("CACheck's err-put: " + e);
			}
			newComps.add(c);
		}
		return newComps;
	}
}
