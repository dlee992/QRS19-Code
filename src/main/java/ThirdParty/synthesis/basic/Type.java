package ThirdParty.synthesis.basic;

public class Type {
	public VarType varType;
	public IOType ioType;

	private Type(VarType varType) {
		this.varType = varType;
	}

	public Type(VarType varType, IOType ioType) {
		this.varType = varType;
		this.ioType = ioType;
	}

	public String toString() {
		return varType + ":" + ioType;
	}

	public static Type intType() {
		return new Type(VarType.INTEGER);
	}
	
	public static Type doubleType() {
		return new Type(VarType.DOUBLE);
	}

	public static Type boolType() {
		return new Type(VarType.BOOLEAN);
	}

	public static Type arrayType() {
		return new Type(VarType.ARRAY);
	}
	
	public static Type hashType() {
		return new Type(VarType.HASH);
	}
}
