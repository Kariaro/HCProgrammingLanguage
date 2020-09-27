package hardcoded.assembly.x86;

import java.util.ArrayList;
import java.util.List;

import hardcoded.utils.StringUtils;

class OprStringBuilder {
	OprStringBuilder() {}
	
	private static final String VALID_CHARACTERS = " []()*+0123456789ABCDEFGHILMPRSTWYXabcdefmoqrtwxy";
	private static final String DELIMITERS = " []()*+";
	
	/**
	 * Validate a input string depending on the valid characters.
	 * 
	 * @param	value	the string to validate
	 * @return {@code true} if the string contains correct characters
	 */
	static boolean validateString(String value) {
		if(value.length() > 255) return false;
		
		for(int i = 0; i < value.length(); i++) {
			if(VALID_CHARACTERS.indexOf(value.charAt(i)) < 0) {
				System.out.println();
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Split the input string into parts.
	 * 
	 * @param	value	the string to split
	 * @return	an array with parts
	 */
	static String[] splitString(String value) {
		List<String> list = new ArrayList<>();
		String buffer = "";
		
		for(int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			
			if(DELIMITERS.indexOf(c) < 0) {
				buffer += c;
			} else {
				if(!buffer.isEmpty())
					list.add(buffer);
				
				if(!Character.isWhitespace(c))
					list.add(String.valueOf(c));
				
				buffer = "";
			}
		}
		
		if(!buffer.isEmpty())
			list.add(buffer);
		
		return list.toArray(new String[0]);
	}
	
	static int getPointerSize(String[] parts) {
		switch(parts[0]) {
			case "[": return 0;
			case "byte": return 8;
			case "word": return 16;
			case "dword": return 32;
			case "qword": return 64;
			case "xmmword": return 128;
			case "ymmword": return 256;
			default: return -1;
		}
	}
	
	static AsmOpr fromString(OprBuilder builder, String value) {
		// Remove leading spaces.
		value = value.replaceAll("\\s+", " ").trim();
		
		if(!validateString(value))
			throw new AssertionError("Operator contains a invalid number or register \"" + value + "\"");
		
		String[] parts = splitString(value);
		
		/**
		 *-1: Register
		 * 0: Pointer
		 * x: Pointer has size
		 */
		int pointerSize = getPointerSize(parts);
		
		if(pointerSize < 0) {
			return readRegister(builder, parts);
		} else {
			return readMemory(builder, parts, value, pointerSize);
		}
	}
	
	static AsmOpr readMemory(OprBuilder builder, String[] parts, String input, int size) {
		if(parts.length < 3)
			throw new AssertionError("Expected a address but got \"" + input + "\"");
		
		int i = (size > 0 ? 2:0);
		
		if(!parts[i - 1].equals("["))
			throw new AssertionError("Expected opening bracket '[' \"" + input + "\"");
		
		boolean closed = false;
		boolean expect_object = true;
		
		int parenthesis = 0;
		for(; i < parts.length; i++) {
			String part = parts[i];
			
			if(part.equals("[")) throw new AssertionError("Invalid placement of bracket '[' \"" + input + "\"");
			if(part.equals("]")) {
				if(i + 1 != parts.length)
					throw new AssertionError("Closing bracket ']' was not end of input \"" + input + "\"");
				
				closed = true;
				break;
			}
			
			boolean expected_object = expect_object;
			boolean has_object = false;
			
			if(part.equals("(")) {
				parenthesis++;
				if(parenthesis > 1)
					throw new AssertionError("Invalid placement of parenthesis \"" + input + "\"");
				expect_object = true;
				has_object = true;
			} else if(part.equals(")")) {
				parenthesis--;
				if(parenthesis < 0)
					throw new AssertionError("Invalid placement of parenthesis \"" + input + "\"");
				
				expect_object = false;
			} else if(part.equals("+")) {
				builder.add();
				expect_object = true;
			} else if(part.equals("*")) {
				
				if(parts[i - 1].equals(")") || builder.parts.isEmpty() || !(builder.parts.get(builder.parts.size() - 1) instanceof OprPart.Reg))
					throw new AssertionError("Invalid placement of multiplication sign \"" + input + "\"");

				builder.mul();
				expect_object = true;
			} else if(isNumber(part)) {
				builder.num(readNumber(part));
				expect_object = false;
				has_object = true;
			} else {
				builder.reg(RegisterX86.valueOf(part));
				expect_object = false;
				has_object = true;
			}
			
			if(parenthesis < 0 || parenthesis > 1)
				throw new AssertionError("Invalid placement of parenthesis \"" + input + "\"");
			
			if(expected_object != has_object) {
				throw new AssertionError("Invalid placement of character '" + part + "' \"" + input + "\"");
			}
		}
		

		if(parenthesis != 0)
			throw new AssertionError("Parenthesis was never closed \"" + input + "\"");
		
		if(!closed)
			throw new AssertionError("Bracket was never closed \"" + input + "\"");
		
		return builder.ptr(size);
	}
	
	static boolean isNumber(String value) {
		if(value.startsWith("0x"))
			return value.substring(2).replaceAll("[0-9a-zA-Z]", "").isEmpty();
		
		return value.replaceAll("[0-9]", "").isEmpty();
	}
	
	static long readNumber(String value) {
		if(value.startsWith("0x"))
			return Long.parseLong(value.substring(2), 16);
		
		return Long.parseLong(value);
	}
	
	static AsmOpr readRegister(OprBuilder builder, String[] parts) {
		if((parts.length != 1))
			throw new AssertionError("Expected a register or immediate value but got \"" + StringUtils.join(" ", parts) + "\"");
		
		if(isNumber(parts[0]))
			return builder.imm(readNumber(parts[0]));
		
		RegisterX86 reg = RegisterX86.valueOf(parts[0]);
		if(reg == null)
			throw new AssertionError("Expected a register but got \"" + StringUtils.join(" ", parts) + "\"");
		
		return builder.reg(reg).get();
	}
}