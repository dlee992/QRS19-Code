package thirdparty.synthesis.state;

import java.util.List;

import com.microsoft.z3.Expr;

public class StateInstance {

	public StateAbstract stateAbstract = null;
	public List<Expr> stateVars = null;

	public StateInstance(StateAbstract sa) {
		this.stateAbstract = sa;
	}

	public StateInstance(StateAbstract sa, List<Expr> states) {
		this.stateAbstract = sa;
		this.stateVars = states;
	}

}
