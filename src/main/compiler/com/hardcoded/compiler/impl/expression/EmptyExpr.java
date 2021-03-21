package com.hardcoded.compiler.impl.expression;

import java.util.Collections;
import java.util.List;

import com.hardcoded.compiler.api.Expression;

/**
 * A empty expression
 * 
 * @author HardCoded
 * @since 0.2.0
 */
public class EmptyExpr implements Expression {
	private EmptyExpr() {
		
	}
	
	@Override
	public Type getType() {
		return Type.NOP;
	}

	@Override
	public List<Expression> getExpressions() {
		return Collections.emptyList();
	}
	
	@Override
	public String toString() {
		return ";";
	}
	
	private static final EmptyExpr EMPTY = new EmptyExpr();
	
	/**
	 * Returns a singleton {@code EMPTY} expression.
	 * @return a singleton {@code EMPTY} expression
	 */
	public static EmptyExpr get() {
		return EMPTY;
	}
	
	public static boolean isEmpty(Expression part) {
		return part == EMPTY;
	}
}
