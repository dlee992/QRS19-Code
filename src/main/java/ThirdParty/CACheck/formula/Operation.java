package ThirdParty.CACheck.formula;

public class Operation {
	public final static int PLUS = 1;
	public final static int MINUS = 2;
	public final static int MULT = 3;
	public final static int DIVIDE = 4;
	public final static int CONSTANT = 5;
	public final static int SUM = 6;

	public static int priority(String op) {
		int priority;

		switch (op.charAt(0)) {
		case '+':
			priority = 1;
			break;
		case '-':
			priority = 1;
			break;
		case '*':
			priority = 2;
			break;
		case '/':
			priority = 2;
			break;
		default:
			priority = 0;
			break;
		}

		return priority;
	}
}
