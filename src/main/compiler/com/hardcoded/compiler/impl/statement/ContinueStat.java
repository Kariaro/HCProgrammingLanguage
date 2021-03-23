package com.hardcoded.compiler.impl.statement;

import com.hardcoded.compiler.lexer.Token;

/**
 * A continue statement
 * 
 * <pre>
 * Valid syntax:
 *   'continue' ';'
 * </pre>
 * 
 * @author HardCoded
 * @since 0.2.0
 */
public class ContinueStat extends Stat {
	private ContinueStat(Token token) {
		super(token);
	}
	
	@Override
	public Type getType() {
		return Type.CONTINUE;
	}
	
	@Override
	public String toString() {
		return "continue;";
	}
	
	public static ContinueStat get(Token token) {
		return new ContinueStat(token);
	}
}
