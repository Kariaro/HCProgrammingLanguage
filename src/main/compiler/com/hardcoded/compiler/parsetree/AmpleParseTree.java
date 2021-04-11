package com.hardcoded.compiler.parsetree;

import com.hardcoded.compiler.api.Expression;
import com.hardcoded.compiler.api.Statement;
import com.hardcoded.compiler.impl.context.Reference;
import com.hardcoded.compiler.impl.statement.*;
import com.hardcoded.compiler.lexer.AmpleLexer;
import com.hardcoded.compiler.lexer.Lang;
import com.hardcoded.compiler.lexer.Token;
import com.hardcoded.logger.Log;
import com.hardcoded.options.Options;

/**
 * Generates a parse tree from a file
 * 
 * @author HardCoded
 * @since 0.2.0
 */
public class AmpleParseTree {
	@SuppressWarnings("unused")
	private static final Log LOGGER = Log.getLogger(AmpleParseTree.class);
	
	public AmpleParseTree() {
		
	}
	
	// This file will generate a output tree struction that contains the syntax tree
	// The output data will contain a list of exported symbols (provided)
	// and a list of imported symbols (missing)
	
	protected AmpleExprParser expression_parser;
	protected Options options;
	protected Lang lang;
	public ProgramStat process(Options options, Lang lang) {
		this.expression_parser = new AmpleExprParser();
		this.options = options;
		this.lang = lang;
		
		ProgramStat stat = begin();
		
		/*
		LOGGER.debug("Root: %s", stat);
		
		String str = TreeUtils.printTree(stat).replace("\t", "    ");
		System.out.println("################################################");
		System.out.println(str);
		System.out.println("################################################");
		*/
		
		return stat;
	}
	
	public ProgramStat process(Options options, byte[] bytes) {
		this.expression_parser = new AmpleExprParser();
		this.options = options;
		this.lang = Lang.wrap(AmpleLexer.getLexer().parse(bytes));
		
		ProgramStat stat = begin();
		
		LOGGER.debug("Root: %s", stat);
		
		String str = TreeUtils.printTree(stat).replace("\t", "    ");
		System.out.println("################################################");
		System.out.println(str);
		System.out.println("################################################");
		
		return stat;
	}
	
	public ProgramStat begin() {
		ProgramStat root = ProgramStat.get();
		
		while(lang.hasNext()) {
			int start_index = lang.readerIndex();
			
			if(isType()) {
				Token type = lang.next();
				if(!isName()) {
					throw_exception("Expected a valid name but got '%s'", lang.value());
				}
				Token name = lang.next();
				
				if(lang.valueEquals("(")) {
					// FUNCTION
					root.add(makeFunction(type, name));
					// LOGGER.debug("function [%s]", root.last());
					continue;
				}
				
				if(lang.valueEquals("=") || lang.valueEquals(";")) {
					// DEFINE
					root.add(makeDefine(type, name));
					// LOGGER.debug("define [%s]", root.last());
					continue;
				}
				
			} else {
				// CLASS
				// IMPORT
				
				if(lang.valueEquals("class")) {
					Token token = lang.next();
					if(!isName()) throw_exception("Expected '[name]' but got '%s'", lang.value());
					root.add(makeClass(token, lang.next()));
				} else if(lang.valueEquals("import")) {
					root.add(makeImport(lang.next(), lang.next()));
					continue;
				} else {
					throw_exception("Expected either 'class' or 'import' but got '%s'", lang.value());
				}
			}
			
			if(start_index == lang.readerIndex()) {
				throw_stuck_exception();
			}
		}
		
		return root;
	}
	
	ClassStat makeClass(Token token, Token name) {
		ClassStat stat = ClassStat.get(token, name);
		// ';' or [class-body]
		if(lang.valueEquals(";")) {
			return stat.end(lang.next());
		}
		
		if(lang.valueEquals("{")) {
			lang.next();
			// [class-body]
			
			while(lang.hasNext()) {
				int start_index = lang.readerIndex();
				
				if(lang.valueEquals(name.value)) {
					Token token_name = lang.next();
					FuncStat constructor = makeFunction(token_name, token_name);
					stat.add(constructor);
				} else {
					if(isType()) {
						Token token_type = lang.next();
						if(!isName()) {
							throw_exception("Expected a valid name but got '%s'", lang.value());
						}
						Token token_name = lang.next();
						
						if(lang.valueEquals("(")) {
							// FUNCTION
							stat.add(makeFunction(token_type, token_name));
							continue;
						}
						
						if(lang.valueEquals("=") || lang.valueEquals(";")) {
							// DEFINE
							stat.add(makeDefine(token_type, token_name));
							continue;
						}
					}
				}
				
				if(lang.valueEquals("}")) {
					break;
				}
				
				if(start_index == lang.readerIndex()) {
					throw_stuck_exception();
				}
			}
			
			check_or_throw("}");
			return stat.end(lang.peek(-1));
		}
		
		throw_exception("Not implemented");
		return stat;
	}

	// Starts at [string]
	ImportStat makeImport(Token token, Token path) {
		ImportStat stat = ImportStat.get(token, path);
		check_or_throw(";");
		return stat.end(lang.peek(-1));
	}
	
	// Starts at '('
	FuncStat makeFunction(Token type, Token name) {
		FuncStat stat = FuncStat.get(type, name);
		
		check_or_throw("(");
		// [args] or ')'
		if(lang.valueEquals(")")) {
			lang.next();
			// ';' or [stat]
		} else {
			// [args]
			while(true) {
				// [type]
				if(!isType()) throw_exception("Expected '[type]' but got '%s'", lang.value());
				Token arg_type = lang.next();
				
				// [name]
				if(!isName()) throw_exception("Expected '[name]' but got '%s'", lang.value());
				Token arg_name = lang.next();
				
				stat.addArgument(makeArgument(arg_type, arg_name));
				if(lang.valueEquals(")")) {
					lang.next();
					// ';' or '{'
					break;
				}
				
				if(lang.valueEquals(",")) {
					lang.next();
					// [arg]
					continue;
				}
				
				throw_exception("Expected ',' or ')' but got '%s'", lang.value());
			}
		}
		
		// ';' or '{'
		if(lang.valueEquals(";")) {
			lang.next();
		} else if(lang.valueEquals("{")) {
			// [stat]
			stat.add(makeStatement());
		} else {
			return throw_exception("Expected ';' or '{' but got '%s'", lang.value());
		}
		
		return stat.end(lang.peek(-1));
	}
	
	// Used in functions
	DefineStat makeArgument(Token type, Token name) {
		return DefineStat.get(type, name);
	}
	
	// Starts at '=', ';'
	DefineStat makeDefine(Token type, Token name) {
		DefineStat stat = DefineStat.get(type, name);
		
		// '=' or ';'
		if(lang.valueEquals("=")) {
			lang.next();
			// [expr]
			stat.add(makeExprStat(true));
		} else {
			stat.add(EmptyStat.get());
		}
		
		check_or_throw(";");
		return stat.end(lang.peek(-1));
	}
	
	// Used inside functions
	Statement makeStatement() {
		// '{' or [stat]
		if(lang.valueEquals("{")) {
			ScopeStat stat = ScopeStat.get(lang.next());
			
			if(lang.valueEquals("}")) {
				lang.next();
				return stat;
			}
			
			// [stat]
			while(lang.hasNext()) {
				int start_index = lang.readerIndex();
				
				Statement part = makeStatement();
				if(!EmptyStat.isEmpty(part)) {
					stat.add(part);
				}
				
				if(lang.valueEquals("}")) break;
				
				if(start_index == lang.readerIndex()) {
					throw_stuck_exception();
				}
			}
			
			check_or_throw("}");
			return stat.end(lang.peek(-1));
		}
		
		if(lang.valueEquals(";")) {
			lang.next();
			return EmptyStat.get();
		}
		
		if(lang.valueEquals("return")) return makeReturn(lang.next());
		if(lang.valueEquals("continue")) return makeContinue(lang.next());
		if(lang.valueEquals("break")) return makeBreak(lang.next());
		if(lang.valueEquals("goto")) return makeGoto(lang.next());
		if(lang.valueEquals("while")) return makeWhile(lang.next());
		if(lang.valueEquals("do")) return makeDoWhile(lang.next());
		if(lang.valueEquals("for")) return makeFor(lang.next());
		if(lang.valueEquals("if")) return makeIf(lang.next());
		if(lang.valueEquals("switch")) return makeSwitch(lang.next());
		
		if(isType()) {
			Token def_type = lang.next();
			if(!isName()) throw_exception("Expected '[name]' but got '%s'", lang.value());
			Token def_name = lang.next();
			return makeDefine(def_type, def_name);
		}
		
		if(isName() && lang.peek(1).valueEquals(":")) return makeLabel(lang.next());
		
		return makeExprStat();
	}
	
	ReturnStat makeReturn(Token token) {
		ReturnStat stat = ReturnStat.get(token);
		
		// ';' or [expr]
		if(lang.valueEquals(";")) {
			return stat.add(EmptyStat.get()).end(lang.next());
		}
		
		// [expr]
		stat.add(makeExprStat(true));
		check_or_throw(";");
		return stat.end(lang.peek(-1));
	}
	
	LabelStat makeLabel(Token token) {
		LabelStat stat = LabelStat.get(token);
		stat.setReference(Reference.get(token.value, Reference.Type.LABEL));
		check_or_throw(":");
		return stat.end(lang.peek(-1));
	}
	
	ContinueStat makeContinue(Token token) {
		ContinueStat stat = ContinueStat.get(token);
		check_or_throw(";");
		return stat.end(lang.peek(-1));
	}
	
	BreakStat makeBreak(Token token) {
		BreakStat stat = BreakStat.get(token);
		check_or_throw(";");
		return stat.end(lang.peek(-1));
	}
	
	GotoStat makeGoto(Token token) {
		Token label = lang.next();
		GotoStat stat = GotoStat.get(token, label);
		stat.setReference(Reference.get(label.value, Reference.Type.LABEL));
		check_or_throw(";");
		return stat.end(lang.peek(-1));
	}
	
	WhileStat makeWhile(Token token) {
		WhileStat stat = WhileStat.get(token);
		
		check_or_throw("(");
		stat.add(makeExprStat(true));
		check_or_throw(")");
		
		// ';' or [stat]
		if(lang.valueEquals(";")) {
			lang.next();
			stat.add(EmptyStat.get());
			return stat;
		}
		
		// [stat]
		stat.add(makeStatement());
		return stat.end(lang.peek(-1));
	}
	
	DoWhileStat makeDoWhile(Token token) {
		DoWhileStat stat = DoWhileStat.get(token);
		// [stat]
		stat.add(makeStatement());
		check_or_throw("while");
		check_or_throw("(");
		// [expr]
		stat.add(makeExprStat(true));
		check_or_throw(")");
		check_or_throw(";");
		return stat.end(lang.peek(-1));
	}
	
	ForStat makeFor(Token token) {
		ForStat stat = ForStat.get(token);
		
		check_or_throw("(");
		if(lang.valueEquals(";")) stat.add(EmptyStat.get());
		else {
			if(isType()) {
				Token type = lang.next();
				if(!isName()) throw_exception("Expected '[name]' but got '%s'", lang.value());
				Token name = lang.next();
				stat.add(makeDefine(type, name));
			} else {
				stat.add(makeExprStat());
			}
		}
		
		if(lang.valueEquals(";")) stat.add(EmptyStat.get());
		else stat.add(makeExprStat());
		
		if(lang.valueEquals(")")) stat.add(EmptyStat.get());
		else stat.add(makeExprStat(true));
		check_or_throw(")");
		
		stat.add(makeStatement());
		return stat.end(lang.peek(-1));
	}
	
	IfStat makeIf(Token token) {
		IfStat stat = IfStat.get(token);
		check_or_throw("(");
		stat.add(makeExprStat(true));
		check_or_throw(")");
		stat.add(makeStatement());
		if(lang.valueEquals("else")) {
			lang.next();
			stat.add(makeStatement());
		}
		
		return stat.end(lang.peek(-1));
	}
	
	SwitchStat makeSwitch(Token token) {
		SwitchStat stat = SwitchStat.get(token);
		check_or_throw("(");
		stat.add(makeExprStat(true));
		check_or_throw(")");
		check_or_throw("{");
		
		while(true) {
			int start_index = lang.readerIndex();
			if(lang.valueEquals("}")) break;
			stat.add(makeCase(lang.next()));
			
			if(start_index == lang.readerIndex()) {
				throw_stuck_exception();
			}
		}
		
		check_or_throw("}");
		return stat.end(lang.peek(-1));
	}
	
	CaseStat makeCase(Token token) {
		CaseStat stat = CaseStat.get(token);
		ExprStat expr_stat = makeExprStat(true);
		if(!TreeUtils.isPureAtom(expr_stat.getExpression())) {
			throw_exception("Invalid case: A case statement can only contain constant values!");
		}
		
		stat.add(expr_stat);
		check_or_throw(":");
		
		while(true) {
			int start_index = lang.readerIndex();
			
			if(lang.valueEquals("}")
			|| lang.valueEquals("case")) break;
			stat.add(makeStatement());
			
			if(start_index == lang.readerIndex()) {
				throw_stuck_exception();
			}
		}
		
		return stat.end(lang.peek(-1));
	}
	
	ExprStat makeExprStat() { return makeExprStat(false); }
	ExprStat makeExprStat(boolean without_semicolon) {
		ExprStat stat = ExprStat.get(lang.token());
		stat.add(makeExpression());
		if(without_semicolon) return stat;
		check_or_throw(";");
		return stat.end(lang.peek(-1));
	}
	
	Expression makeExpression() { return makeExpression(true); }
	Expression makeExpression(boolean use_comma) {
		return expression_parser.begin(lang, use_comma);
	}
	
	boolean isName() { return isName(0); }
	boolean isName(int offset) {
		return lang.peek(offset).groupEquals("IDENTIFIER");
	}
	
	boolean isType() { return isType(0); }
	boolean isType(int offset) {
		String value = lang.peek(offset).value;
		
		// Have a list with known classes and check here
		return value.equals("void")
			|| value.equals("str")
			|| value.equals("num");
	}
	
	<T> T throw_exception(String format, Object... args) {
		String extra = String.format("(line: %d, column: %d) ", lang.line(), lang.column());
		throw new ParseTreeException(extra + format, args);
	}
	
	<T> T throw_invalid_exception() {
		String extra = String.format("(line: %d, column: %d) ", lang.line(), lang.column());
		throw new ParseTreeException(extra + "Invalid syntax:%d", getLineIndex());
	}
	
	<T> T throw_stuck_exception() {
		String extra = String.format("(line: %d, column: %d) ", lang.line(), lang.column());
		String contn = String.format("[%s] %s", lang.peekString(-5, 5), lang.peekString(0, 10));
		throw new ParseTreeException(extra + "Compiler got stuck on line:%d\n%s", getLineIndex(), contn);
	}
	
	<T> T check_or_throw(String value) {
		if(lang.valueEquals(value)) {
			lang.next();
			return null;
		}
		
		String extra = String.format("(line: %d, column: %d) ", lang.line(), lang.column());
		throw new ParseTreeException(extra + "Expected '%s' but got '%s'", value, lang.value());
	}
	
	<T> T check_or_throw(String value, T result) {
		if(lang.valueEquals(value)) {
			lang.next();
			return result;
		}
		
		String extra = String.format("(line: %d, column: %d) ", lang.line(), lang.column());
		throw new ParseTreeException(extra + "Expected '%s' but got '%s'", value, lang.value());
	}
	
	int getLineIndex() {
		StackTraceElement[] stack = Thread.getAllStackTraces().get(Thread.currentThread());
		if(stack == null) return -1;
		StackTraceElement last = stack[stack.length - 3];
		return last.getLineNumber();
	}
}