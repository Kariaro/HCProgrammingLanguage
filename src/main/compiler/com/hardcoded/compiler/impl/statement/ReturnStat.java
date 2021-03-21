package com.hardcoded.compiler.impl.statement;

import java.util.ArrayList;
import java.util.List;

import com.hardcoded.compiler.api.Statement;
import com.hardcoded.compiler.lexer.Token;

/**
 * A return statement
 * 
 * <pre>
 * Valid syntax:
 *   'return' [expr] ';'
 *   'return' ';'
 * </pre>
 * 
 * @author HardCoded
 * @since 0.2.0
 */
public class ReturnStat implements Statement {
	protected final List<Statement> list;
	protected final Token token;
	
	private ReturnStat(Token token) {
		this.list = new ArrayList<>();
		this.token = token;
	}
	
	@Override
	public Type getType() {
		return Type.RETURN;
	}

	@Override
	public List<Statement> getStatements() {
		return list;
	}

	@Override
	public int getLineIndex() {
		return token.line;
	}

	@Override
	public int getColumnIndex() {
		return token.column;
	}
	
	public void add(Statement stat) {
		list.add(stat);
	}
	
	@Override
	public String toString() {
		if(list.isEmpty()) return "return;";
		return String.format("return %s;", list);
	}
	
	public static ReturnStat get(Token token) {
		return new ReturnStat(token);
	}
}
