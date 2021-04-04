package com.hardcoded.compiler.impl.statement;

import com.hardcoded.compiler.impl.context.IRefContainer;
import com.hardcoded.compiler.impl.context.Reference;
import com.hardcoded.compiler.lexer.Token;

/**
 * A define statment
 * 
 * <pre>
 * Valid syntax:
 *   [type] [name] '=' [expr] ';'
 *   [type] [name] ';'
 * </pre>
 * 
 * @author HardCoded
 * @since 0.2.0
 */
public class DefineStat extends Stat implements IRefContainer {
	protected final Token type;
	protected final Token name;
	protected Reference ref;
	
	private DefineStat(Token type, Token name) {
		super(type, true);
		this.type = type;
		this.name = name;
		this.ref = Reference.get(name.value, Reference.Type.VAR);
	}
	
	@Override
	public Type getType() {
		return Type.DEFINE;
	}
	
	public Token getValueType() {
		return type;
	}
	
	public Token getName() {
		return name;
	}
	
	@Override
	public Reference getReference() {
		return ref;
	}
	
	@Override
	public void setReference(Reference ref) {
		this.ref = ref;
	}
	
	@Override
	public Token getRefToken() {
		return name;
	}
	
	@Override
	public String toString() {
		if(list.isEmpty()) {
			return String.format("%s %s;", type, name);
		}
		
		return String.format("%s %s = %s;", type, name, list.get(0));
	}
	
	public static DefineStat get(Token type, Token name) {
		return new DefineStat(type, name);
	}
}
