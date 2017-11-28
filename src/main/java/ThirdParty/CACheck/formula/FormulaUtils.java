package ThirdParty.CACheck.formula;

import java.util.List;

import ThirdParty.CACheck.amcheck.AnalysisPattern;
import ThirdParty.synthesis.component.Component;

public class FormulaUtils {
	// TODO
	public static boolean isUnsupported(String formula) {
		// deal with simple pattern
		if (AnalysisPattern.simple) {
			if (formula.toLowerCase().contains("max")
					|| formula.toLowerCase().contains("min")
					|| formula.toLowerCase().contains("average")) {
				return true;
			}
		}
		if (formula.contains("!")
				//TODO
				|| formula.toLowerCase().contains(",,")
				|| formula.toLowerCase().contains(", ,") 
				|| formula.toLowerCase().contains("(,")  
				|| formula.toLowerCase().contains(",)")  
				
				|| formula.toLowerCase().contains("if")

				|| formula.toLowerCase().contains("abs") // we can support it.
				|| formula.toLowerCase().contains("and")
				|| formula.toLowerCase().contains("apr")
				|| formula.toLowerCase().contains("atan")
				|| formula.toLowerCase().contains("averagea")
				|| formula.toLowerCase().contains("avedev")
				|| formula.toLowerCase().contains("betainv")
				|| formula.toLowerCase().contains("binomdist")
				|| formula.toLowerCase().contains("ceiling")
				|| formula.toLowerCase().contains("cell")
				|| formula.toLowerCase().contains("chidist")
				|| formula.toLowerCase().contains("choose")
				|| formula.toLowerCase().contains("concatenate")
				|| formula.toLowerCase().contains("confidence")
				|| formula.toLowerCase().contains("countblank")
				|| formula.toLowerCase().contains("column")
				|| formula.toLowerCase().contains("covar")
				|| formula.toLowerCase().contains("count")
				|| formula.toLowerCase().contains("counta")
				|| formula.toLowerCase().contains("correl")
				|| formula.toLowerCase().contains("cos")
				|| formula.toLowerCase().contains("cumprinc")
				|| formula.toLowerCase().contains("daverage")
				|| formula.toLowerCase().contains("date")
				|| formula.toLowerCase().contains("day")
				//|| formula.toLowerCase().contains("db")
				|| formula.toLowerCase().contains("dcount")
				|| formula.toLowerCase().contains("dcounta")
				|| formula.toLowerCase().contains("ddb")
				|| formula.toLowerCase().contains("dget")
				|| formula.toLowerCase().contains("dmax")
				|| formula.toLowerCase().contains("dmin")
				|| formula.toLowerCase().contains("dollarde")
				|| formula.toLowerCase().contains("dollarfr")
				|| formula.toLowerCase().contains("dproduct")
				|| formula.toLowerCase().contains("dstdev")
				|| formula.toLowerCase().contains("dstdevp")
				|| formula.toLowerCase().contains("dsum")
				|| formula.toLowerCase().contains("dvar")
				|| formula.toLowerCase().contains("dvarp")
				|| formula.toLowerCase().contains("effect")
				|| formula.toLowerCase().contains("exact")
				|| formula.toLowerCase().contains("eomonth")
				|| formula.toLowerCase().contains("even")
				|| formula.toLowerCase().contains("exp")
				|| formula.toLowerCase().contains("find")
				|| formula.toLowerCase().contains("frequency")
				|| formula.toLowerCase().contains("fv")
				|| formula.toLowerCase().contains("forecast")
				|| formula.toLowerCase().contains("getpivotdata")

				|| formula.toLowerCase().contains("hlookup")
				|| formula.toLowerCase().contains("hour")
				|| formula.toLowerCase().contains("irr")
				|| formula.toLowerCase().contains("int")
				|| formula.toLowerCase().contains("index")
				|| formula.toLowerCase().contains("indirect")
				|| formula.toLowerCase().contains("inv")
				|| formula.toLowerCase().contains("iserror")
				|| formula.toLowerCase().contains("intercept")
				|| formula.toLowerCase().contains("large")
				|| formula.toLowerCase().contains("last")
				|| formula.toLowerCase().contains("len")
				|| formula.toLowerCase().contains("left")
				|| formula.toLowerCase().contains("linest")
				|| formula.toLowerCase().contains("lookup")
				|| formula.toLowerCase().contains("log")
				|| formula.toLowerCase().contains("ln")
				|| formula.toLowerCase().contains("maxa")
				|| formula.toLowerCase().contains("match")
				|| formula.toLowerCase().contains("median")
				|| formula.toLowerCase().contains("mid")
				|| formula.toLowerCase().contains("minute")
				|| formula.toLowerCase().contains("mina")
				|| formula.toLowerCase().contains("minverse")
				|| formula.toLowerCase().contains("month")
				|| formula.toLowerCase().contains("mode")
				|| formula.toLowerCase().contains("mmult")
				|| formula.toLowerCase().contains("n/a")
				|| formula.toLowerCase().contains("na")
				|| formula.toLowerCase().contains("noerror")
				|| formula.toLowerCase().contains("normdist")
				|| formula.toLowerCase().contains("normsdist")
				|| formula.toLowerCase().contains("normsinv")
				|| formula.toLowerCase().contains("now")
				|| formula.toLowerCase().contains("nper")

				|| formula.toLowerCase().contains("offset")
				|| formula.toLowerCase().contains("paj")
				|| formula.toLowerCase().contains("pi")
				|| formula.toLowerCase().contains("pmt")
				|| formula.toLowerCase().contains("power")
				|| formula.toLowerCase().contains("project")
				|| formula.toLowerCase().contains("pv")
				|| formula.toLowerCase().contains("product")
				|| formula.toLowerCase().contains("quartile")
				|| formula.toLowerCase().contains("q_")
				|| formula.toLowerCase().contains("rand")
				|| formula.toLowerCase().contains("radians")
				|| formula.toLowerCase().contains("rate")
				|| formula.toLowerCase().contains("rank")
				|| formula.toLowerCase().contains("rept")
				|| formula.toLowerCase().contains("right")
				|| formula.toLowerCase().contains("risk")
				|| formula.toLowerCase().contains("row")
				|| formula.toLowerCase().contains("round")
				|| formula.toLowerCase().contains("rv")
				|| formula.toLowerCase().contains("sign")
				|| formula.toLowerCase().contains("sin")
				|| formula.toLowerCase().contains("skew")
				|| formula.toLowerCase().contains("splinefit")
				|| formula.toLowerCase().contains("sumif")
				|| formula.toLowerCase().contains("sumproduct")
				|| formula.toLowerCase().contains("subtotal")
				|| formula.toLowerCase().contains("slope")
				|| formula.toLowerCase().contains("stdev")
				|| formula.toLowerCase().contains("small")
				|| formula.toLowerCase().contains("sqrt")
				|| formula.toLowerCase().contains("stdevp")
				|| formula.toLowerCase().contains("syd")
				|| formula.toLowerCase().contains("time")
				|| formula.toLowerCase().contains("tdist")
				|| formula.toLowerCase().contains("transpose")
				|| formula.toLowerCase().contains("trunc")
				|| formula.toLowerCase().contains("today")
				|| formula.toLowerCase().contains("tan")

				|| formula.toLowerCase().contains("vlookup")
				|| formula.toLowerCase().contains("vdb")
				|| formula.toLowerCase().contains("var")
				|| formula.toLowerCase().contains("varp")
				|| formula.toLowerCase().contains("weekday")
				|| formula.toLowerCase().contains("year")
				|| formula.toLowerCase().contains("^")) {
			return true;

		} else {
			return false;
		}
	}

	public static boolean isSupported(List<Component> comps) {
		for (Component comp : comps) {
			if (comp instanceof UnsupportedComponent) {
				return false;
			}
		}
		return true;
	}
}
