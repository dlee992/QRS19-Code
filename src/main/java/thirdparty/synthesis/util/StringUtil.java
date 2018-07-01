package thirdparty.synthesis.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import thirdparty.synthesis.basic.Type;
import thirdparty.synthesis.basic.VarType;

import com.microsoft.z3.ArithExpr;
import com.microsoft.z3.ArrayExpr;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.IntExpr;
import com.microsoft.z3.IntNum;
import com.microsoft.z3.Model;
import com.microsoft.z3.Z3Exception;

public class StringUtil {

	public static int maxLen = 6;

	public static BoolExpr lengthConstraint(Context ctx, ArrayExpr arr,
			IntExpr len) throws Z3Exception {

		IntExpr nullPos = len;
		ArithExpr lastPos = ctx.mkAdd(nullPos, ctx.mkInt(-1));
		BoolExpr lastEle = ctx.mkNot(ctx.mkEq(ctx.mkSelect(arr, lastPos),
				ctx.mkInt(0)));
		BoolExpr nullEle = ctx.mkEq(ctx.mkSelect(arr, nullPos), ctx.mkInt(0));
		BoolExpr lenIsNot0 = ctx.mkAnd(lastEle, nullEle);

		BoolExpr lenIs0 = ctx.mkAnd(
				ctx.mkEq(ctx.mkSelect(arr, ctx.mkInt(0)), ctx.mkInt(0)),
				ctx.mkEq(len, ctx.mkInt(0)));

		BoolExpr lenGE0 = ctx.mkGe(len, ctx.mkInt(0));

		// TODO extra length constraint
		BoolExpr extraLen = ctx.mkLt(len, ctx.mkInt(maxLen));

		return ctx.mkAnd(ctx.mkOr(lenIsNot0, lenIs0), lenGE0, extraLen);
	}

	public static BoolExpr substringConstraint(Context ctx, ArrayExpr arr,
			IntExpr i, IntExpr j, ArrayExpr retArr) throws Z3Exception {

		IntExpr k = ctx.mkIntConst(Z3Util.getVarName());

		BoolExpr kCons = ctx.mkAnd(ctx.mkGe(k, i), ctx.mkLt(k, j));

		BoolExpr eleEqual = ctx.mkEq(ctx.mkSelect(arr, k),
				ctx.mkSelect(retArr, ctx.mkAdd(k, ctx.mkUnaryMinus(i))));

		BoolExpr allEleEqual = ctx.mkForall(new Expr[] { k },
				ctx.mkImplies(kCons, eleEqual), 1, null, null, null, null);

		return allEleEqual;
	}

	public static BoolExpr substringConstraintBound(Context ctx, ArrayExpr arr,
			IntExpr i, IntExpr j, ArrayExpr retArr) throws Z3Exception {

		BoolExpr substr = ctx.mkFalse();

		for (int ii = 0; ii < maxLen; ii++) {
			BoolExpr iCons = ctx.mkEq(i, ctx.mkInt(ii));

			for (int jj = ii; jj < maxLen; jj++) {
				BoolExpr jCons = ctx.mkEq(j, ctx.mkInt(jj));

				BoolExpr eleCons = ctx.mkTrue();
				for (int index = ii; index < jj; index++) {
					Expr ele1 = ctx.mkSelect(arr, ctx.mkInt(index));
					Expr ele2 = ctx.mkSelect(retArr, ctx.mkInt(index - ii));
					eleCons = ctx.mkAnd(eleCons, ctx.mkEq(ele1, ele2));
				}

				BoolExpr cons = ctx.mkAnd(iCons, jCons, eleCons);

				substr = ctx.mkOr(substr, cons);
			}
		}
		return substr;
	}

	public static BoolExpr stringAssignConstraint(Context ctx, ArrayExpr arrA,
			IntExpr lenA, IntExpr start, ArrayExpr arrB, IntExpr lenB,
			ArrayExpr arrRet) throws Z3Exception {

		IntExpr index1 = ctx.mkIntConst(Z3Util.getVarName());
		IntExpr index2 = ctx.mkIntConst(Z3Util.getVarName());
		IntExpr index3 = ctx.mkIntConst(Z3Util.getVarName());

		BoolExpr index1Cons = ctx.mkAnd(ctx.mkGe(index1, ctx.mkInt(0)),
				ctx.mkLt(index1, start));
		BoolExpr index2Cons = ctx.mkAnd(ctx.mkGe(index2, start),
				ctx.mkLt(index2, ctx.mkAdd(start, lenB)));
		BoolExpr index3Cons = ctx.mkAnd(
				ctx.mkGe(index3, ctx.mkAdd(start, lenB)),
				ctx.mkLt(index3, lenA));

		BoolExpr eleEqual1 = ctx.mkEq(ctx.mkSelect(arrA, index1),
				ctx.mkSelect(arrRet, index1));
		BoolExpr eleEqual2 = ctx.mkEq(
				ctx.mkSelect(arrB, ctx.mkAdd(index2, ctx.mkUnaryMinus(start))),
				ctx.mkSelect(arrRet, index2));
		BoolExpr eleEqual3 = ctx.mkEq(ctx.mkSelect(arrA, index3),
				ctx.mkSelect(arrRet, index3));

		BoolExpr allEleEqual1 = ctx
				.mkForall(new Expr[] { index1 },
						ctx.mkImplies(index1Cons, eleEqual1), 1, null, null,
						null, null);
		BoolExpr allEleEqual2 = ctx
				.mkForall(new Expr[] { index2 },
						ctx.mkImplies(index2Cons, eleEqual2), 1, null, null,
						null, null);
		BoolExpr allEleEqual3 = ctx
				.mkForall(new Expr[] { index3 },
						ctx.mkImplies(index3Cons, eleEqual3), 1, null, null,
						null, null);

		return ctx.mkAnd(allEleEqual1, allEleEqual2, allEleEqual3);
	}

	public static BoolExpr stringAssignConstraintBound(Context ctx,
			ArrayExpr arrA, IntExpr lenA, IntExpr start, ArrayExpr arrB,
			IntExpr lenB, ArrayExpr arrRet) throws Z3Exception {

		BoolExpr assignCons = ctx.mkFalse();
		for (int arrALen = 0; arrALen <= maxLen; arrALen++) {
			BoolExpr lenACons = ctx.mkEq(lenA, ctx.mkInt(arrALen));
			for (int star = 0; star <= arrALen; star++) {
				BoolExpr startCons = ctx.mkEq(start, ctx.mkInt(star));
				for (int arrBLen = 0; arrBLen + star <= maxLen; arrBLen++) {
					BoolExpr lenBCons = ctx.mkEq(lenB, ctx.mkInt(arrBLen));

					BoolExpr equal = ctx.mkTrue();
					for (int i = 0; i < star; i++) {
						Expr ele1 = ctx.mkSelect(arrA, ctx.mkInt(i));
						Expr ele2 = ctx.mkSelect(arrRet, ctx.mkInt(i));
						equal = ctx.mkAnd(equal, ctx.mkEq(ele1, ele2));
					}

					for (int i = star; i < star + arrBLen; i++) {
						Expr ele1 = ctx.mkSelect(arrB, ctx.mkInt(i - star));
						Expr ele2 = ctx.mkSelect(arrRet, ctx.mkInt(i));
						equal = ctx.mkAnd(equal, ctx.mkEq(ele1, ele2));
					}

					for (int i = star + arrBLen; i < arrALen; i++) {
						Expr ele1 = ctx.mkSelect(arrA, ctx.mkInt(i));
						Expr ele2 = ctx.mkSelect(arrRet, ctx.mkInt(i));
						equal = ctx.mkAnd(equal, ctx.mkEq(ele1, ele2));
					}

					BoolExpr cons = ctx.mkAnd(lenACons, startCons, lenBCons,
							equal);
					assignCons = ctx.mkOr(assignCons, cons);
				}
			}
		}
		return assignCons;
	}

	public static BoolExpr containConstraint(Context ctx, ArrayExpr arr1,
			IntExpr len1, ArrayExpr arr2, IntExpr len2) throws Z3Exception {

		IntExpr index1 = ctx.mkIntConst(Z3Util.getVarName());
		IntExpr index2 = ctx.mkIntConst(Z3Util.getVarName());

		BoolExpr index1Cons = ctx.mkAnd(ctx.mkGe(index1, ctx.mkInt(0)),
				ctx.mkLe(index1, len1));

		BoolExpr index2Cons = ctx.mkAnd(ctx.mkGe(index2, ctx.mkInt(0)),
				ctx.mkLt(index2, len2));

		BoolExpr eleEqual = ctx.mkEq(
				ctx.mkSelect((ArrayExpr) arr1, ctx.mkAdd(index1, index2)),
				ctx.mkSelect((ArrayExpr) arr2, index2));

		BoolExpr allEleEqual = ctx.mkForall(new Expr[] { index2 },
				ctx.mkImplies(index2Cons, eleEqual), 1, null, null, null, null);

		BoolExpr index1Exist = ctx.mkExists(new Expr[] { index1 },
				ctx.mkAnd(index1Cons, allEleEqual), 1, null, null, null, null);

		return index1Exist;
	}

	public static BoolExpr containConstraintBound(Context ctx, ArrayExpr arr1,
			IntExpr len1, ArrayExpr arr2, IntExpr len2) throws Z3Exception {

		BoolExpr contain = ctx.mkFalse();
		for (int arr1Len = 1; arr1Len <= maxLen; arr1Len++) {
			BoolExpr len1Cons = ctx.mkEq(len1, ctx.mkInt(arr1Len));

			for (int arr2Len = 1; arr2Len <= arr1Len; arr2Len++) {
				BoolExpr len2Cons = ctx.mkEq(len2, ctx.mkInt(arr2Len));

				BoolExpr existEqual = ctx.mkFalse();
				for (int index = 0; index <= arr1Len - arr2Len; index++) {
					BoolExpr eleCons = eleEqual(ctx, arr1, index, arr2, 0, arr2Len);
					existEqual = ctx.mkOr(existEqual, eleCons);
				}

				BoolExpr cons = ctx.mkAnd(len1Cons, len2Cons, existEqual);
				contain = ctx.mkOr(contain, cons);
			}
		}
		return contain;
	}

	public static BoolExpr addConstraintBound(Context ctx, ArrayExpr arr1,
			IntExpr len1, ArrayExpr arr2, IntExpr len2, IntExpr index,
			IntExpr val) throws Z3Exception {

		BoolExpr add = ctx.mkFalse();
		for (int arr1Len = 0; arr1Len < maxLen; arr1Len++) {
			BoolExpr len1Cons = ctx.mkEq(len1, ctx.mkInt(arr1Len));
			for (int i = 0; i <= arr1Len; i++) {
				BoolExpr indexCons = ctx.mkEq(index, ctx.mkInt(i));

				BoolExpr eleCons1 = eleEqual(ctx, arr1, 0, arr2, 0, i);
				BoolExpr addCons = ctx.mkEq(ctx.mkSelect(arr2, index), val);
				BoolExpr eleCons2 = eleEqual(ctx, arr1, i, arr2, i + 1, arr1Len
						- i);

				BoolExpr cons = ctx.mkAnd(len1Cons, indexCons, eleCons1,
						addCons, eleCons2);
				add = ctx.mkOr(add, cons);
			}
		}
		return add;
	}

	public static BoolExpr removeConstraintBound(Context ctx, ArrayExpr arr1,
			IntExpr len1, ArrayExpr arr2, IntExpr len2, IntExpr indexVar)
			throws Z3Exception {

		BoolExpr remove = ctx.mkFalse();
		for (int arr1Len = 1; arr1Len <= maxLen; arr1Len++) {
			BoolExpr len1Cons = ctx.mkEq(len1, ctx.mkInt(arr1Len));

			for (int index = 0; index < arr1Len; index++) {
				BoolExpr iCons = ctx.mkEq(indexVar, ctx.mkInt(index));

				BoolExpr eleCons = ctx.mkTrue();
				for (int i = 0; i < arr1Len; i++) {
					Expr ele1 = null;
					Expr ele2 = null;
					if (i < index) {
						ele1 = ctx.mkSelect(arr1, ctx.mkInt(i));
						ele2 = ctx.mkSelect(arr2, ctx.mkInt(i));
					} else if (i > index) {
						ele1 = ctx.mkSelect(arr1, ctx.mkInt(i));
						ele2 = ctx.mkSelect(arr2, ctx.mkInt(i - 1));
					} else {
						continue;
					}
					eleCons = ctx.mkAnd(eleCons, ctx.mkEq(ele1, ele2));
				}

				BoolExpr cons = ctx.mkAnd(len1Cons, iCons, eleCons);
				remove = ctx.mkOr(remove, cons);
			}
		}
		return remove;
	}

	public static BoolExpr concatConstraint(Context ctx, ArrayExpr arr1,
			IntExpr len1, ArrayExpr arr2, IntExpr len2, ArrayExpr arr,
			IntExpr len) throws Z3Exception {

		IntExpr k1 = ctx.mkIntConst(Z3Util.getVarName());
		IntExpr k2 = ctx.mkIntConst(Z3Util.getVarName());

		BoolExpr indexCons1 = ctx.mkAnd(ctx.mkGe(k1, ctx.mkInt(0)),
				ctx.mkLt(k1, len1));

		BoolExpr indexCons2 = ctx.mkAnd(ctx.mkGe(k2, len1), ctx.mkLt(k2, len));

		BoolExpr equal1 = ctx.mkEq(ctx.mkSelect(arr1, k1),
				ctx.mkSelect(arr, k1));

		BoolExpr equal2 = ctx.mkEq(
				ctx.mkSelect(arr2, ctx.mkAdd(k2, ctx.mkUnaryMinus(len1))),
				ctx.mkSelect(arr, k2));

		BoolExpr allEleEqual1 = ctx.mkForall(new Expr[] { k1 },
				ctx.mkImplies(indexCons1, equal1), 1, null, null, null, null);
		BoolExpr allEleEqual2 = ctx.mkForall(new Expr[] { k2 },
				ctx.mkImplies(indexCons2, equal2), 1, null, null, null, null);

		BoolExpr allEleEqual = ctx.mkAnd(allEleEqual1, allEleEqual2);

		return allEleEqual;
	}

	public static BoolExpr concatConstraintBound(Context ctx, ArrayExpr arr1,
			IntExpr len1, ArrayExpr arr2, IntExpr len2, ArrayExpr arr,
			IntExpr len) throws Z3Exception {

		BoolExpr ret = ctx.mkFalse();
		for (int arr1Len = 0; arr1Len <= maxLen; arr1Len++) {
			BoolExpr len1Cons = ctx.mkEq(len1, ctx.mkInt(arr1Len));

			for (int arr2Len = 0; arr2Len <= maxLen; arr2Len++) {
				BoolExpr len2Cons = ctx.mkEq(len2, ctx.mkInt(arr2Len));

				BoolExpr eleCons = ctx.mkTrue();
				for (int index1 = 0; index1 < arr1Len; index1++) {
					Expr ele1 = ctx.mkSelect(arr1, ctx.mkInt(index1));
					Expr ele2 = ctx.mkSelect(arr, ctx.mkInt(index1));
					eleCons = ctx.mkAnd(eleCons, ctx.mkEq(ele1, ele2));
				}
				for (int index2 = 0; index2 < arr2Len; index2++) {
					Expr ele1 = ctx.mkSelect(arr2, ctx.mkInt(index2));
					Expr ele2 = ctx.mkSelect(arr, ctx.mkInt(arr1Len + index2));
					eleCons = ctx.mkAnd(eleCons, ctx.mkEq(ele1, ele2));
				}

				BoolExpr cons = ctx.mkAnd(len1Cons, len2Cons, eleCons);

				ret = ctx.mkOr(ret, cons);
			}
		}

		return ret;
	}

	public static BoolExpr N2NConstraintBound(Context ctx, ArrayExpr arrInput,
			IntExpr len, ArrayExpr arrRet) throws Z3Exception {

		BoolExpr ret = ctx.mkFalse();
		for (int arrLen = 0; arrLen <= maxLen; arrLen++) {
			BoolExpr lenCons = ctx.mkEq(len, ctx.mkInt(arrLen));

			BoolExpr eleCons = ctx.mkTrue();
			for (int index = 0; index < arrLen; index++) {
				Expr ele1 = ctx.mkSelect(arrInput, ctx.mkInt(index));
				Expr ele2 = ctx.mkSelect(arrRet, ctx.mkInt(index));
				eleCons = ctx.mkAnd(eleCons, ctx.mkEq(ele1, ele2));
			}

			BoolExpr cons = ctx.mkAnd(lenCons, eleCons);

			ret = ctx.mkOr(ret, cons);
		}

		return ret;
	}

	public static BoolExpr N21ConstraintBound(Context ctx, ArrayExpr arrInput,
			IntExpr len, IntExpr sumRet) throws Z3Exception {

		BoolExpr ret = ctx.mkFalse();
		for (int arrLen = 0; arrLen <= maxLen; arrLen++) {
			BoolExpr lenCons = ctx.mkEq(len, ctx.mkInt(arrLen));

			ArithExpr sum = ctx.mkInt(0);
			for (int index = 0; index < arrLen; index++) {
				IntExpr ele = (IntExpr) ctx
						.mkSelect(arrInput, ctx.mkInt(index));
				sum = ctx.mkAdd(sum, ele);
			}

			BoolExpr cons = ctx.mkAnd(lenCons, ctx.mkEq(sum, sumRet));

			ret = ctx.mkOr(ret, cons);
		}

		return ret;
	}

	public static BoolExpr indexOfContainConstraint(Context ctx,
			ArrayExpr arr1, IntExpr len1, ArrayExpr arr2, IntExpr len2,
			IntExpr index) throws Z3Exception {

		IntExpr index2 = ctx.mkIntConst(Z3Util.getVarName());

		BoolExpr indexCons = ctx.mkAnd(ctx.mkGe(index, ctx.mkInt(0)),
				ctx.mkLe(index, len1));

		BoolExpr index2Cons = ctx.mkAnd(ctx.mkGe(index2, ctx.mkInt(0)),
				ctx.mkLt(index2, len2));

		BoolExpr eleEqual = ctx.mkEq(
				ctx.mkSelect((ArrayExpr) arr1, ctx.mkAdd(index, index2)),
				ctx.mkSelect((ArrayExpr) arr2, index2));

		BoolExpr allEleEqual = ctx.mkForall(new Expr[] { index2 },
				ctx.mkImplies(index2Cons, eleEqual), 1, null, null, null, null);

		return ctx.mkAnd(indexCons, allEleEqual);
	}

	public static BoolExpr indexOfConstraint(Context ctx, ArrayExpr arr1,
			IntExpr len1, ArrayExpr arr2, IntExpr len2, IntExpr iMin)
			throws Z3Exception {
		IntExpr index = ctx.mkIntConst(Z3Util.getVarName());

		BoolExpr iMinContainCons = StringUtil.indexOfContainConstraint(ctx,
				arr1, len1, arr2, len2, iMin);
		BoolExpr iMinCons = ctx.mkAnd(iMinContainCons, ctx.mkForall(
				new Expr[] { index }, ctx.mkImplies(StringUtil
						.indexOfContainConstraint(ctx, arr1, len1, arr2, len2,
								index), ctx.mkLe(iMin, index)), 1, null, null,
				null, null));

		return iMinCons;
	}

	public static BoolExpr indexOfConstraintBound(Context ctx, ArrayExpr arr1,
			IntExpr len1, ArrayExpr arr2, IntExpr len2, IntExpr iMin)
			throws Z3Exception {

		BoolExpr indexOf = ctx.mkFalse();
		for (int arr1Len = 1; arr1Len <= maxLen; arr1Len++) {
			BoolExpr len1Cons = ctx.mkEq(len1, ctx.mkInt(arr1Len));

			for (int arr2Len = 1; arr2Len <= arr1Len; arr2Len++) {
				BoolExpr len2Cons = ctx.mkEq(len2, ctx.mkInt(arr2Len));

				BoolExpr existEqual = nexIndexBound(ctx, arr1, arr2, arr2Len,
						0, arr1Len - arr2Len, iMin);

				BoolExpr cons = ctx.mkAnd(len1Cons, len2Cons, existEqual);
				indexOf = ctx.mkOr(indexOf, cons);
			}
		}

		return indexOf;
	}

	public static BoolExpr nexIndexBound(Context ctx, ArrayExpr arr1,
			ArrayExpr arr2, int arr2Len, int index, int maxIndex, IntExpr iMin)
			throws Z3Exception {
		if (index > maxIndex) {
			return ctx.mkEq(iMin, ctx.mkInt(-1));
		}

		BoolExpr eleCons = eleEqual(ctx, arr1, index, arr2, 0, arr2Len);
		return (BoolExpr) ctx.mkITE(
				eleCons,
				ctx.mkEq(iMin, ctx.mkInt(index)),
				nexIndexBound(ctx, arr1, arr2, arr2Len, index + 1, maxIndex,
						iMin));
	}

	public static BoolExpr fillArray0(Context ctx, ArrayExpr arr, int length)
			throws Z3Exception {
		IntExpr k = ctx.mkIntConst(Z3Util.getVarName());

		BoolExpr indexCons = ctx.mkNot(ctx.mkAnd(ctx.mkGe(k, ctx.mkInt(0)),
				ctx.mkLt(k, ctx.mkInt(length))));

		BoolExpr equal0 = ctx.mkEq(ctx.mkSelect(arr, k), ctx.mkInt(0));

		Expr eleEqual0 = ctx.mkImplies(indexCons, equal0);

		BoolExpr allEleEqual0 = ctx.mkForall(new Expr[] { k }, eleEqual0, 1,
				null, null, null, null);

		return allEleEqual0;
	}

	public static BoolExpr fillArray0Bound(Context ctx, ArrayExpr arr,
			int length) throws Z3Exception {

		BoolExpr equalZero = ctx.mkTrue();
		for (int i = length; i <= maxLen; i++) {
			Expr ele = ctx.mkSelect(arr, ctx.mkInt(i));
			Expr zero = ctx.mkInt(0);
			equalZero = ctx.mkAnd(equalZero, ctx.mkEq(ele, zero));
		}

		return equalZero;
	}

	public static BoolExpr fillArray0(Context ctx, ArrayExpr arr,
			ArithExpr length) throws Z3Exception {
		IntExpr k = ctx.mkIntConst(Z3Util.getVarName());

		BoolExpr indexCons = ctx.mkNot(ctx.mkAnd(ctx.mkGe(k, ctx.mkInt(0)),
				ctx.mkLt(k, length)));

		BoolExpr equal0 = ctx.mkEq(ctx.mkSelect(arr, k), ctx.mkInt(0));

		Expr eleEqual0 = ctx.mkImplies(indexCons, equal0);

		BoolExpr allEleEqual0 = ctx.mkForall(new Expr[] { k }, eleEqual0, 1,
				null, null, null, null);

		return allEleEqual0;
	}

	public static BoolExpr fillArray0Bound(Context ctx, ArrayExpr arr,
			ArithExpr length) throws Z3Exception {

		BoolExpr ret = ctx.mkFalse();
		for (int len = 0; len <= maxLen; len++) {
			BoolExpr lenCons;
			if (len == 0) {
				lenCons = ctx.mkLe(length, ctx.mkInt(0));
			} else {
				lenCons = ctx.mkEq(length, ctx.mkInt(len));
			}

			BoolExpr equalZero = ctx.mkTrue();
			for (int i = len; i <= maxLen + 1; i++) {
				Expr ele = ctx.mkSelect(arr, ctx.mkInt(i));
				Expr zero = ctx.mkInt(0);
				equalZero = ctx.mkAnd(equalZero, ctx.mkEq(ele, zero));
			}

			BoolExpr cons = ctx.mkAnd(lenCons, equalZero);

			ret = ctx.mkOr(ret, cons);
		}

		return ret;
	}

	public static BoolExpr wellFormedString(Context ctx, ArrayExpr arr)
			throws Z3Exception {
		IntExpr k = ctx.mkIntConst(Z3Util.getVarName());
		IntExpr gk = ctx.mkIntConst(Z3Util.getVarName());

		BoolExpr indexCons = ctx.mkGt(gk, k);

		BoolExpr equal0 = ctx.mkAnd(ctx.mkGe(k, ctx.mkInt(0)),
				ctx.mkEq(ctx.mkSelect(arr, k), ctx.mkInt(0)));

		Expr gkGt0 = ctx.mkImplies(indexCons,
				ctx.mkEq(ctx.mkSelect(arr, gk), ctx.mkInt(0)));

		BoolExpr allgkGt0 = ctx.mkForall(new Expr[] { gk }, gkGt0, 1, null,
				null, null, null);

		Expr kisTerm = ctx.mkImplies(equal0, allgkGt0);
		BoolExpr allkisTerm = ctx.mkForall(new Expr[] { k }, kisTerm, 1, null,
				null, null, null);

		// TODO extra constraints, can be deleted
		IntExpr i = ctx.mkIntConst(Z3Util.getVarName());
		BoolExpr iCons = ctx.mkAnd(
				ctx.mkGe((IntExpr) ctx.mkSelect(arr, i), ctx.mkInt(0)),
				ctx.mkLt((IntExpr) ctx.mkSelect(arr, i), ctx.mkInt(10)));
		BoolExpr allCons = ctx.mkForall(new Expr[] { i }, iCons, 1, null, null,
				null, null);

		return ctx.mkAnd(allkisTerm, allCons);
	}

	public static BoolExpr wellFormedStringBound(Context ctx, ArrayExpr arr)
			throws Z3Exception {
		BoolExpr ret = ctx.mkFalse();
		for (int len = 0; len < maxLen / 2; len++) {
			BoolExpr eleCons = ctx.mkTrue();
			for (int i = 0; i <= maxLen; i++) {
				Expr ele = ctx.mkSelect(arr, ctx.mkInt(i));
				Expr zero = ctx.mkInt(0);

				if (i < len) {
					eleCons = ctx
							.mkAnd(eleCons, ctx.mkNot(ctx.mkEq(ele, zero)));
				} else {
					eleCons = ctx.mkAnd(eleCons, ctx.mkEq(ele, zero));
				}
			}

			ret = ctx.mkOr(ret, eleCons);
		}

		// TODO extra constraints, can be deleted
		for (int i = 0; i <= maxLen; i++) {
			BoolExpr iCons = ctx.mkAnd(
					ctx.mkGe((IntExpr) ctx.mkSelect(arr, ctx.mkInt(i)),
							ctx.mkInt(0)),
					ctx.mkLt((IntExpr) ctx.mkSelect(arr, ctx.mkInt(i)),
							ctx.mkInt(10)));
			ret = ctx.mkAnd(ret, iCons);
		}

		return ret;
	}

	public static BoolExpr editDistance(Context ctx, ArrayExpr arr1,
			ArrayExpr arr2) throws Z3Exception {

		IntExpr len1 = ctx.mkIntConst(Z3Util.getVarName());
		IntExpr len2 = ctx.mkIntConst(Z3Util.getVarName());

		BoolExpr len1Cons = StringUtil.lengthConstraint(ctx, arr1, len1);
		BoolExpr len2Cons = StringUtil.lengthConstraint(ctx, arr2, len2);

		BoolExpr lenCons = ctx.mkOr(ctx.mkEq(len1, len2),
				ctx.mkEq(len1, ctx.mkAdd(len2, ctx.mkInt(1))));

		// len1 = len2
		IntExpr index1 = ctx.mkIntConst(Z3Util.getVarName());
		IntExpr neIndex1 = ctx.mkIntConst(Z3Util.getVarName());

		BoolExpr neIndexCons1 = ctx.mkAnd(
				ctx.mkGe(neIndex1, ctx.mkInt(0)),
				ctx.mkLt(neIndex1, len1),
				ctx.mkNot(ctx.mkEq(ctx.mkSelect(arr1, neIndex1),
						ctx.mkSelect(arr2, neIndex1))));

		BoolExpr eIndexCons1 = ctx.mkAnd(ctx.mkGe(index1, ctx.mkInt(0)),
				ctx.mkLt(index1, len1), ctx.mkNot(ctx.mkEq(index1, neIndex1)));
		BoolExpr eCons1 = ctx.mkEq(ctx.mkSelect(arr1, index1),
				ctx.mkSelect(arr2, index1));

		BoolExpr offOneCons1 = ctx.mkForall(new Expr[] { index1 },
				ctx.mkImplies(eIndexCons1, eCons1), 1, null, null, null, null);
		offOneCons1 = ctx.mkAnd(neIndexCons1, offOneCons1);

		// len1 = len2 + 1
		IntExpr index2 = ctx.mkIntConst(Z3Util.getVarName());
		IntExpr neIndex2 = ctx.mkIntConst(Z3Util.getVarName());

		BoolExpr neIndexCons2 = ctx.mkAnd(
				ctx.mkGe(neIndex2, ctx.mkInt(0)),
				ctx.mkLt(neIndex2, len1),
				ctx.mkNot(ctx.mkEq(ctx.mkSelect(arr1, neIndex2),
						ctx.mkSelect(arr2, neIndex2))));

		BoolExpr eIndexCons2 = ctx.mkAnd(ctx.mkGe(index2, ctx.mkInt(0)),
				ctx.mkLt(index2, len1), ctx.mkNot(ctx.mkEq(index2, neIndex2)));
		BoolExpr eCons2 = ctx.mkOr(
				ctx.mkAnd(
						ctx.mkLt(index2, neIndex2),
						ctx.mkEq(ctx.mkSelect(arr1, index2),
								ctx.mkSelect(arr2, index2))),
				ctx.mkAnd(
						ctx.mkGt(index2, neIndex2),
						ctx.mkEq(
								ctx.mkSelect(arr1, index2),
								ctx.mkSelect(arr2,
										ctx.mkAdd(index2, ctx.mkInt(-1))))));

		BoolExpr offOneCons2 = ctx.mkForall(new Expr[] { index2 },
				ctx.mkImplies(eIndexCons2, eCons2), 1, null, null, null, null);
		offOneCons2 = ctx.mkAnd(neIndexCons2, offOneCons2);

		BoolExpr offOneCons = ctx.mkOr(ctx.mkAnd(ctx.mkEq(len1, len2),
				offOneCons1), ctx.mkAnd(
				ctx.mkNot(ctx.mkAnd(ctx.mkEq(len1, len2))), offOneCons2));

		return ctx.mkAnd(len1Cons, len2Cons, lenCons, offOneCons);
	}

	public static BoolExpr editDistanceBound(Context ctx, ArrayExpr arr1,
			ArrayExpr arr2) throws Z3Exception {

		IntExpr len1 = ctx.mkIntConst(Z3Util.getVarName());
		IntExpr len2 = ctx.mkIntConst(Z3Util.getVarName());

		BoolExpr len1Cons = StringUtil.lengthConstraint(ctx, arr1, len1);
		BoolExpr len2Cons = StringUtil.lengthConstraint(ctx, arr2, len2);

		BoolExpr lenCons = ctx.mkOr(ctx.mkEq(len1, len2),
				ctx.mkEq(len1, ctx.mkAdd(len2, ctx.mkInt(1))));
		lenCons = ctx.mkAnd(len1Cons, len2Cons, lenCons);

		// len1 = len2
		BoolExpr offOneCons = ctx.mkFalse();
		for (int len = 1; len < maxLen / 2; len++) {
			BoolExpr l1Cons = ctx.mkEq(len1, ctx.mkInt(len));
			BoolExpr l2Cons = ctx.mkEq(len2, ctx.mkInt(len));

			for (int neIndex = 0; neIndex < len; neIndex++) {
				BoolExpr eleSCons = ctx.mkTrue();
				for (int index = 0; index < len; index++) {
					Expr ele1 = ctx.mkSelect(arr1, ctx.mkInt(index));
					Expr ele2 = ctx.mkSelect(arr2, ctx.mkInt(index));

					BoolExpr eleCons = null;
					if (index != neIndex) {
						eleCons = ctx.mkEq(ele1, ele2);
					} else {
						eleCons = ctx.mkNot(ctx.mkEq(ele1, ele2));
					}
					eleSCons = ctx.mkAnd(eleSCons, eleCons);
				}
				offOneCons = ctx.mkOr(offOneCons,
						ctx.mkAnd(l1Cons, l2Cons, eleSCons));
			}
		}

		// len1 = len2 + 1
		for (int len = 1; len < maxLen / 2; len++) {
			BoolExpr l1Cons = ctx.mkEq(len1, ctx.mkInt(len));
			BoolExpr l2Cons = ctx.mkEq(len2, ctx.mkInt(len - 1));

			for (int neIndex = 0; neIndex < len; neIndex++) {
				BoolExpr eleSCons = ctx.mkTrue();
				for (int index = 0; index < len; index++) {
					Expr ele1 = ctx.mkSelect(arr1, ctx.mkInt(index));

					BoolExpr eleCons = null;
					if (index < neIndex) {
						Expr ele2 = ctx.mkSelect(arr2, ctx.mkInt(index));
						eleCons = ctx.mkEq(ele1, ele2);
					} else if (index > neIndex) {
						Expr ele2 = ctx.mkSelect(arr2, ctx.mkInt(index - 1));
						eleCons = ctx.mkEq(ele1, ele2);
					}
					if (eleCons != null) {
						eleSCons = ctx.mkAnd(eleSCons, eleCons);
					}
				}
				offOneCons = ctx.mkOr(offOneCons,
						ctx.mkAnd(l1Cons, l2Cons, eleSCons));
			}
		}

		return ctx.mkAnd(lenCons, offOneCons);
	}

	public static int editDistance(int[] arr1, int[] arr2) {
		if (arr1.length < arr2.length) {
			int[] temp = arr1;
			arr1 = arr2;
			arr2 = temp;
		}

		int len1 = arr1.length;
		int len2 = arr2.length;

		if (len1 > len2 + 1) {
			return 2;
		}

		int neNum = 0;

		if (len1 == len2) {
			for (int i = 0; i < len1; i++) {
				if (arr1[i] != arr2[i])
					neNum++;
			}
		} else {
			for (int i1 = 0, i2 = 0; i1 < len1 && i2 < len2;) {
				if (arr1[i1] != arr2[i2]) {
					neNum++;
					i1++;
				} else {
					i1++;
					i2++;
				}
			}
			if (neNum == 0) {
				neNum++;
			}
		}

		return neNum;
	}

	public static Map<Integer, int[]> explainHash(Context ctx, Model model,
			ArrayExpr expr) throws Z3Exception {

		Map<Integer, int[]> map = new HashMap<Integer, int[]>();
		for (int key = 0; key < StringUtil.maxLen; key++) {
			ArrayExpr keyValue = (ArrayExpr) model.eval(
					ctx.mkSelect(expr, ctx.mkInt(key)), true);

			List<Integer> values = new ArrayList<Integer>();
			for (int i = 0; i < StringUtil.maxLen; i++) {
				Expr iValue = model.eval(ctx.mkSelect(keyValue, ctx.mkInt(i)),
						true);
				int value = ((IntNum) iValue).getInt();
				if (value != 0) {
					values.add(value);
				} else {
					break;
				}
			}
			int[] arr = new int[values.size()];
			for (int i = 0; i < arr.length; i++) {
				arr[i] = values.get(i);
			}
			map.put(key, arr);
		}

		return map;
	}

	public static int[] explainArray(Context ctx, Model model, ArrayExpr expr)
			throws Z3Exception {
		List<Integer> res = new ArrayList<Integer>();
		for (int i = 0;; i++) {
			Expr iValue = model.eval(ctx.mkSelect(expr, ctx.mkInt(i)), true);
			int value = ((IntNum) iValue).getInt();
			if (value != 0) {
				res.add(value);
			} else {
				break;
			}
		}

		int[] arr = new int[res.size()];
		for (int i = 0; i < arr.length; i++) {
			arr[i] = res.get(i);
		}

		return arr;
	}

	public static BoolExpr eleEqual(Context ctx, ArrayExpr arr1, int start1,
			ArrayExpr arr2, int start2, int size) throws Z3Exception {
		BoolExpr eleCons = ctx.mkTrue();
		for (int i = 0; i < size; i++) {
			Expr ele1 = ctx.mkSelect(arr1, ctx.mkInt(start1 + i));
			Expr ele2 = ctx.mkSelect(arr2, ctx.mkInt(start2 + i));
			eleCons = ctx.mkAnd(eleCons, ctx.mkEq(ele1, ele2));
		}
		return eleCons;
	}

	public static BoolExpr equal(Context ctx, Type type, Expr exp1, Expr exp2)
			throws Z3Exception {
		if (type == null) {
			return ctx.mkBool(true);
		}
		if (type.varType == VarType.ARRAY) {
			ArrayExpr arr1 = (ArrayExpr) exp1;
			ArrayExpr arr2 = (ArrayExpr) exp2;
			BoolExpr es = ctx.mkTrue();
			for (int i = 0; i < StringUtil.maxLen; i++) {
				IntExpr ie = ctx.mkInt(i);
				BoolExpr e = ctx.mkEq(ctx.mkSelect(arr1, ie),
						ctx.mkSelect(arr2, ie));
				es = ctx.mkAnd(es, e);
			}
			return es;
		} else if (type.varType == VarType.HASH) {
			ArrayExpr hash1 = (ArrayExpr) exp1;
			ArrayExpr hash2 = (ArrayExpr) exp2;

			BoolExpr es = ctx.mkTrue();
			for (int key = 0; key < StringUtil.maxLen; key++) {
				ArrayExpr value1 = (ArrayExpr) ctx.mkSelect(hash1,
						ctx.mkInt(key));
				ArrayExpr value2 = (ArrayExpr) ctx.mkSelect(hash2,
						ctx.mkInt(key));

				for (int i = 0; i < StringUtil.maxLen; i++) {
					IntExpr ie = ctx.mkInt(i);
					BoolExpr e = ctx.mkEq(ctx.mkSelect(value1, ie),
							ctx.mkSelect(value2, ie));
					es = ctx.mkAnd(es, e);
				}
			}
			return es;
		} else {
			return ctx.mkEq(exp1, exp2);
		}
	}
}
