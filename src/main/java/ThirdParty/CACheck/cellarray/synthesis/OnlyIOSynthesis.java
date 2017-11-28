package ThirdParty.CACheck.cellarray.synthesis;

import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.List;

import ThirdParty.CACheck.CellArray;
import ThirdParty.CACheck.AMSheet;
import ThirdParty.CACheck.cellarray.extract.CAResult;
import ThirdParty.CACheck.cellarray.inference.Constraints;
import ThirdParty.CACheck.cellarray.inference.Lval2Formula;
import ThirdParty.CACheck.formula.ParseFormula;
import ThirdParty.CACheck.util.Log;
import ThirdParty.synthesis.basic.IOPair;
import ThirdParty.synthesis.basic.ProgramAbstract;
import ThirdParty.synthesis.basic.Result;
import ThirdParty.synthesis.basic.Type;
import ThirdParty.synthesis.io.IOSynthesis;

import com.microsoft.z3.Z3Exception;

public class OnlyIOSynthesis extends IOSynthesis {
	private AMSheet sheet = null;

	public OnlyIOSynthesis(AMSheet sheet) {
		this.sheet = sheet;
		this.sheet.getClass();
	}

	public List<Object> doSynthesis(CAResult car, BufferedWriter writer) {
		Constraints cons = car.constraints;
		CellArray ca = car.cellArray;

		// print all the constraints
		SynthesisUtils.printConstraints(cons, writer);

		List<Type> inputTypes = new ArrayList<Type>();
		for (int i = 0; i < cons.getInputs().size(); i++) {
			inputTypes.add(Type.intType());
		}

		List<IOPair> pairs = cons.getIOPairs();

		Log.logNewLine("Synthesize starting....", writer);

		SynthesisUtils.printComponents(cons.getComponents(), writer);
		try {
			ProgramAbstract program = new ProgramAbstract(cons.getComponents(),
					inputTypes, Type.intType());
			program.init(ctx);

			List<IOPair> curPairs = new ArrayList<IOPair>();
			Result res = null;
			for (int i = 0; i < pairs.size(); i++) {
				curPairs.add(pairs.get(i));

				SynthesisUtils.printIOPairs(curPairs, writer);

				res = generateProgram(program, curPairs);
				if (res == null) {
					curPairs.remove(curPairs.size() - 1);
					continue;
				} else {
					Object[] newInputs = generateDistinctProgram(program,
							curPairs);
					if (newInputs == null) {
						break;
					}
				}
			}

			String formula = null;
			List<Object> pattern = null;
			if (res != null) {
				formula = Lval2Formula.tranform(ca, res, program,
						cons.getInputs(), true);
				int row = 0;
				int col = 0;
				if (ca.isRowCA) {
					row = ca.rowOrColumn;
					col = ca.start;
				} else {
					row = ca.start;
					col = ca.rowOrColumn;
				}
				pattern = ParseFormula.parse(ca, row, col, formula);
			} else {
				Log.logNewLine("No Formula!", writer);
			}

			Log.logNewLine("Synthesize ending....", writer);
			Log.logNewLine("", writer);

			return pattern;
		} catch (Z3Exception e) {
			Log.log(e.toString(), writer);
		}

		return null;
	}
}
