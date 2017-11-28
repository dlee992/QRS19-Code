package ThirdParty.synthesis.state;

import java.util.List;

import com.microsoft.z3.Expr;

import ThirdParty.synthesis.basic.Type;

public class StateAbstract {

	public List<Type> stateTypes = null;

	public StateAbstract(List<Type> stateTypes) {
		this.stateTypes = stateTypes;
	}

	public StateInstance getInstance() {
		return new StateInstance(this);
	}
	
	public StateInstance getInstance(List<Expr> states) {
		return new StateInstance(this, states);
	}
}
