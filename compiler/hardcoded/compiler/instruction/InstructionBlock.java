package hardcoded.compiler.instruction;

import hardcoded.compiler.Block.Function;
import hardcoded.compiler.expression.LowType;

public class InstructionBlock {
	public IRInstruction start;
	public LowType returnType;
	public String name;
	public String extra;
	
	public InstructionBlock(String name, IRInstruction inst) {
		this.name = name;
		this.start = inst;
	}
	
	public InstructionBlock(String name, LowType returnType, IRInstruction inst) {
		this.returnType = returnType;
		this.name = name;
		this.start = inst;
	}
	
	public InstructionBlock(Function func, IRInstruction inst) {
		this.returnType = func.returnType.type();
		this.name = func.name;
		this.start = inst;
	}
}
