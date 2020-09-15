package hardcoded.compiler.parsetree;

import static hardcoded.compiler.Expression.ExprType.*;

import java.util.ArrayList;
import java.util.List;

import hardcoded.compiler.*;
import hardcoded.compiler.Block.Function;
import hardcoded.compiler.Expression.AtomExpr;
import hardcoded.compiler.Expression.ExprType;
import hardcoded.compiler.Expression.OpExpr;
import hardcoded.compiler.Statement.ForStat;
import hardcoded.compiler.Statement.IfStat;
import hardcoded.compiler.Statement.WhileStat;
import hardcoded.compiler.constants.AtomType;
import hardcoded.compiler.constants.Utils;
import hardcoded.compiler.expression.ExpressionParser;
import hardcoded.visualization.Visualization;

public class ParseTreeOptimizer {
	
	// TODO: Figure out a better name than 'do_constant_folding'
	// TODO: Convert strings into decptr(<ptr to string>);
	// TODO: Only if string is const otherwise stack....
	// TODO: cor and cand has problems with some or operations...
	
	public ParseTreeOptimizer() {
		
	}

	public void do_constant_folding(Program current_program) {
		do_constant_folding(Visualization.DUMMY, current_program);
	}
	
	public void do_constant_folding(Visualization vs, Program current_program) {
		for(int i = 0; i < current_program.size(); i++) {
			Block block = current_program.get(i);
			
			if(!(block instanceof Function)) continue;
			Function func = (Function)block;
			
			Utils.execute_for_all_expressions(func, (parent, index, function) -> {
				String bef = "" + parent.get(index);
				constantFolding(parent, index, function);
				
				String now = "" + parent.get(index);
				
				if(!bef.equals(now)) {
					vs.show(current_program);
					vs.getComponent().repaint();
					try {
						// Thread.sleep(1000);
					} catch(Exception e) {
						e.printStackTrace();
					}
					
					// System.out.println("[" + index + "] (" + bef + ")\n[" + index + "] (" + now + ")\n");
				}
			});
			
			Utils.execute_for_all_statements(func, (parent, index, function) -> {
				Statement stat = parent.get(index);
				
				if(stat instanceof ForStat) {
					Expression c = ((ForStat)stat).condition();
					if(c instanceof AtomExpr) {
						AtomExpr a = (AtomExpr)c;
						if(a.isNumber() && a.isZero()) parent.set(index, Statement.EMPTY);
					}
				}
				
				if(stat instanceof WhileStat) {
					Expression c = ((WhileStat)stat).condition();
					if(c instanceof AtomExpr) {
						AtomExpr a = (AtomExpr)c;
						if(a.isNumber() && a.isZero()) parent.set(index, Statement.EMPTY);
					}
				}
				
				if(stat instanceof IfStat) {
					IfStat is = (IfStat)stat;
					Expression c = is.condition();
					if(c instanceof AtomExpr) {
						AtomExpr a = (AtomExpr)c;
						if(a.isNumber()) {
							if(a.isZero()) {
								if(is.elseBody() == null) {
									parent.set(index, Statement.EMPTY);
								} else {
									parent.set(index, is.elseBody());
								}
							} else {
								parent.set(index, is.body());
							}
						}
					}
				}
			});
		}
	}
	
	private void constantFolding(List<Expression> parent, int index, Function func) {
		Expression expr = parent.get(index);
		// System.out.println("Folding: [" + func.name + "], [" + expr + "]");
		
		/* If the left hand side of the expression is a comma	[ ( ... , x) ]
		 * then the assignment operation should be placed only
		 * on the last element of that comma expression.
		 * 
		 * An example would be that the expression 	[ ( ... , x) += 5 ]
		 * should become 							[ ( ... , x += 5) ]
		 */
		if(expr instanceof OpExpr) {
			OpExpr e = (OpExpr)expr;
			
			/* If the type of the expression is one of	[ add, sub, cor, cand, comma ]
			 * then the expression can be expanded.
			 * 
			 * Expanding an expression is to remove nested operations and put them
			 * into one expression. 
			 * 
			 * An example of this expansion would be	[ add(1, add(2, add(3, 4))) ]
			 * where it can be expanded into			[ add(1, 2, 3, 4) ]
			 * 
			 * Expanding expression will make it easier for the compiler to convert
			 * to machine code later.
			 */
			if(e.type == add || e.type == sub || e.type == cor || e.type == cand || e.type == comma) {
				for(int i = e.size() - 1; i >= 0; i--) {
					Expression ex = e.get(i);
					if(ex instanceof OpExpr) {
						OpExpr nx = (OpExpr)ex;
						
						if(nx.type == e.type) {
							e.list.remove(i);
							e.list.addAll(i, nx.list);
							i += nx.list.size();
							continue;
						}
					}
				}
			}
	
			switch(e.type) {
				case set: {
					Expression a = e.first();
					Expression b = e.last();
					
					boolean hasComma = a.type() == comma || b.type() == comma;
					
					if(hasComma) {
						/* Folding the expression			[ set(comma( AAA , x), comma( BBB , y)) ]
						 * 
						 * The comma expression has already removed all elements that does not
						 * have side effects or expanded the comma expression if there was only one
						 * element inside it. This means that if we see a comma expression then we
						 * need to change it to				[ comma( AAA , BBB , set(x, y)) ]
						 */
						OpExpr o = new OpExpr(comma);
						
						if(a.type() == comma) {
							/* Folding the expression		[ set(comma( AAA , x), y) ]
							 * into							[ comma( AAA , set(x, y)) ] */
							for(int i = 0; i < a.size() - 1; i++) o.add(a.get(i));
							e.set(0, a.last());
						}
						
						if(b.type() == comma) {
							/* Folding the expression		[ set(x, comma( BBB , y)) ]
							 * into							[ comma( BBB , set(x, y)) ] */
							for(int i = 0; i < b.size() - 1; i++) o.add(b.get(i));
							e.set(1, b.last());
						}
						
						o.add(e);
						
						// Change the expression into the new format.
						parent.set(index, o);
					}
					
					break;
				}
				
				case comma: {
					for(int i = 0; i < e.size() - 1; i++) {
						if(!e.get(i).hasSideEffects()) e.list.remove(i--);
					}
					
					if(e.size() == 1) parent.set(index, e.first());
					break;
				}
				
				case addptr: case decptr: {
					ExprType opp = e.type == decptr ? addptr:decptr;
					
					if(e.first().type() == opp) {
						parent.set(index, e.first().first());
					} else {
						Expression ex = e.first();
						AtomType last = ex.calculateSize();
						// System.out.println("[" + ex + "] -> " + last + "(" + e + ": " + e.type + ")");
						AtomType size;
						
						if(last != null && last.isPointer()) {
							size = AtomType.getPointer(last, opp == decptr ? 1:-1);
						} else {
							size = last;
						}
						
						if(ex instanceof OpExpr) {
							((OpExpr)ex).override_size = size;
						} else {
							((AtomExpr)ex).override_size = size;
						}
					}
					
					break;
				}
				
				case sub: case add: {
					List<AtomExpr> list = new ArrayList<>();
					for(int i = 0; i < e.size(); i++) {
						Expression e0 = e.get(i);
						
						if(e0 instanceof AtomExpr) {
							AtomExpr a = (AtomExpr)e0;
							
							if(a.isNumber()) {
								list.add(a);
								e.list.remove(i);
								i--;
							} else {
								//throw new RuntimeException("You cannot add a non number value");
							}
						}
					}
					
					if(!list.isEmpty()) {
						for(; list.size() > 1;) {
							AtomExpr c = (AtomExpr)ExpressionParser.compute(e.type, list.get(0), list.get(1));
							list.remove(0);
							list.set(0, c);
						}
						
						if(!(list.get(0).isZero() && e.size() > 0)) {
							e.list.add(list.get(0));
						}
					}
					
					if(e.size() == 1) parent.set(index, e.first());
					break;
				}
				
				case cand: {
					/* Folding the cand operation.
					 * 
					 * The expression		[ cand(x, y) ]
					 */
					for(int i = 0; i < e.size(); i++) {
						Expression e0 = e.get(i);
						
						if(e0.type() == comma) {
							/* Folding the expression	[ cand(x, comma( ... )) ] */
							
							if(e0.last() instanceof AtomExpr) {
								AtomExpr a = (AtomExpr)e0.last();
								
								if(a.isZero()) {
									for(; i + 1 < e.size(); ) {
										e.list.remove(i + 1);
									}
								} else {
									((OpExpr)e0).set(e0.size() - 1, new AtomExpr(1));
									if(i < e.size() - 1) e.list.remove(i--);
								}
							}
						}
						
						if(e0 instanceof AtomExpr) {
							AtomExpr a = (AtomExpr)e0;
							
							if(a.isNumber()) {
								if(a.isZero()) {
									for(; i + 1 < e.size(); ) {
										e.list.remove(i + 1);
									}
								} else {
									if(i < e.size() - 1) e.list.remove(i--);
								}
							}
						}
					}
					
					if(e.size() == 1) {
						if(e.first() instanceof AtomExpr) {
							parent.set(index, new AtomExpr(((AtomExpr)e.first()).isZero() ? 0:1));
							break;
						}
						
						parent.set(index, e.first());
					}
					break;
				}
				
				// TODO: Still will fail for [ b = (0 || a) ] because it will become [ b = a ]
				case cor: {
					for(int i = 0; i < e.size(); i++) {
						Expression e0 = e.get(i);
						
						if(e0.type() == comma) {
							/* Folding the expression	[ cor(x, comma( ... )) ] */
							
							if(e0.last() instanceof AtomExpr) {
								AtomExpr a = (AtomExpr)e0.last();
								
								if(!a.isZero()) {
									// Replace the number with a one....
									((OpExpr)e0).set(e0.size() - 1, new AtomExpr(1));
									for(; i + 1 < e.size(); ) {
										e.list.remove(i + 1);
									}
								} else {
									if(i < e.size() - 1) e.list.remove(i--);
								}
							}
						}
						
						if(e0 instanceof AtomExpr) {
							AtomExpr a = (AtomExpr)e0;
							
							if(a.isNumber()) {
								if(!a.isZero()) {
									for(; i + 1 < e.size(); ) {
										e.list.remove(i + 1);
									}
								} else {
									if(i < e.size() - 1) e.list.remove(i--);
								}
							}
						}
					}
					
					if(e.size() == 1) {
						if(e.first() instanceof AtomExpr) {
							parent.set(index, new AtomExpr(((AtomExpr)e.first()).isZero() ? 0:1));
							break;
						}
						
						parent.set(index, e.first());
					}
					break;
				}
				
				case neg: case not:
				case mul: case div:
				case nor: case xor:
				case shr: case shl:
				case or: case and:
				case lt: case lte:
				case gt: case gte:
				case eq: case neq:
				case mod: {
					Expression next = ExpressionParser.compute(e.type, e);
					if(next != null) parent.set(index, next); break;
				}
				
				case ret:
				case call: {
					/* There is nothing we can do here.
					 */
					break;
				}
				
				default: {
					System.out.println("[NOT FOLDED]" + e);
				}
			}
		}
	}
}
