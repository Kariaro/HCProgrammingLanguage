package hardcoded.compiler.expression;

import hardcoded.compiler.Expression;
import hardcoded.compiler.Expression.AtomExpr;
import hardcoded.compiler.Expression.ExprType;
import hardcoded.compiler.Expression.OpExpr;
import hardcoded.compiler.constants.AtomType;

// TODO: Signed unsigned?
public final class ExpressionParser {
	private ExpressionParser() {}
	
	private static final boolean isNumbers(Expression... array) {
		for(Expression e : array) if(!isNumber(e)) return false;
		return true;
	}
	
	private static final boolean isNumber(Expression a) {
		if(!(a instanceof AtomExpr)) return false;
		return ((AtomExpr)a).isNumber();
	}
	
	@FunctionalInterface
	private static interface BiFnc {
		public default AtomExpr run(AtomExpr a, AtomExpr b) { return o(a.i_value, b.i_value).convert(AtomType.largest(a.atomType, b.atomType)); }
		AtomExpr o(long a, long b);
	}
	
	@FunctionalInterface
	private static interface UnFnc {
		public default AtomExpr run(AtomExpr a) { return o(a.i_value).convert(a.atomType); }
		AtomExpr o(long a);
	}
	
	
	// Unsigned > Signed
	
	private static final BiFnc ADD = (a, b) -> { return new AtomExpr(a + b); };
	private static final BiFnc SUB = (a, b) -> { return new AtomExpr(a - b); };
	private static final BiFnc MUL = (a, b) -> { return new AtomExpr(a * b); };
	private static final BiFnc DIV = (a, b) -> { return new AtomExpr(a / b); };
	private static final BiFnc MOD = (a, b) -> { return new AtomExpr(a % b); };
	
	/* unsigned safe */ private static final BiFnc XOR = (a, b) -> { return new AtomExpr(a ^ b); };
	/* unsigned safe */ private static final BiFnc AND = (a, b) -> { return new AtomExpr(a & b); };
	/* unsigned safe */ private static final BiFnc OR = (a, b) -> { return new AtomExpr(a | b); };
	
	private static final BiFnc SHR = (a, b) -> { return new AtomExpr(a >>> b); };
	private static final BiFnc SHL = (a, b) -> { return new AtomExpr(a << b); };
	
	/* unsigned safe */ private static final BiFnc EQ = (a, b) -> { return new AtomExpr(a == b ? 1:0); };
	/* unsigned safe */ private static final BiFnc NEQ = (a, b) -> { return new AtomExpr(a != b ? 1:0); };
	
	private static final BiFnc LT = (a, b) -> { return new AtomExpr(a < b ? 1:0); };
	private static final BiFnc LTE = (a, b) -> { return new AtomExpr(a <= b ? 1:0); };
	private static final BiFnc GT = (a, b) -> { return new AtomExpr(a > b ? 1:0); };
	private static final BiFnc GTE = (a, b) -> { return new AtomExpr(a >= b ? 1:0); };
	
	/* unsigned safe */
	@SuppressWarnings("unused") private static final BiFnc CAND = (a, b) -> { return new AtomExpr(((a != 0) && (b != 0)) ? 1:0); };
	@SuppressWarnings("unused") private static final BiFnc COR = (a, b) -> { return new AtomExpr(((a != 0) || (b != 0)) ? 1:0); };
	
	/* unsigned safe */ private static final UnFnc NOR = (a) -> { return new AtomExpr(~a); };
	/* unsigned safe */ private static final UnFnc NOT = (a) -> { return new AtomExpr(a == 0 ? 1:0); };
	
	// Does not change unsigned values.
	/* Should throw error becuase other wise the coder could mistake the expression.
	 * [(3 - -(unsigned)4)] as 7.
	 * 
	 * This is complicated....
	 */
	private static final UnFnc NEG = (a) -> { return new AtomExpr(-a); };
	
	
	
	public static Expression compute(ExprType type, OpExpr e) {
		return compute(type, e.first(), e.last());
	}
	
	public static Expression compute(ExprType type, Expression e0, Expression e1) {
		if(!isNumbers(e0, e1)) return null;
		AtomExpr a = (AtomExpr)e0;
		AtomExpr b = (AtomExpr)e1;
		
		// TODO: Cand, cor.
		switch(type) {
			case neg: return NEG.run(a);
			case nor: return NOR.run(a);
			case not: return NOT.run(a);
			
			case mul: return MUL.run(a, b);
			case div: return DIV.run(a, b);
			case mod: return MOD.run(a, b);

			case add: return ADD.run(a, b); // TODO: Contains multiple values sometimes
			case sub: return SUB.run(a, b); // TODO: Contains multiple values sometimes
			
			case xor: return XOR.run(a, b);
			
			case or: return OR.run(a, b);
			case and: return AND.run(a, b);
			case shr: return SHR.run(a, b);
			case shl: return SHL.run(a, b);
			
			case eq: return EQ.run(a, b);
			case neq: return NEQ.run(a, b);
			case lt: return LT.run(a, b);
			case lte: return LTE.run(a, b);
			case gt: return GT.run(a, b);
			case gte: return GTE.run(a, b);
			
			default: return null;
		}
	}
}
