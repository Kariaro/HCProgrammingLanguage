package hardcoded.exporter.impl;

import hardcoded.compiler.instruction.IRProgram;

public interface CodeGeneratorImpl {
	
	/**
	 * Returns a output byte array from a input {@code IRProgram}.
	 * 
	 * @param	program	the {@code IRProgram} to export
	 * @return	a byte array
	 * @throws	CodeGenException
	 */
	public byte[] generate(IRProgram program) throws CodeGenException;
	
	/**
	 * Called uppon reseting the code generator.
	 */
	public void reset();
}
