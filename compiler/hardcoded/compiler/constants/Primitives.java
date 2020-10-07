package hardcoded.compiler.constants;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import hardcoded.compiler.types.PrimitiveType;
import hardcoded.compiler.types.HighType;

/**
 * This is a container class for all primitive data types.
 * @author HardCoded
 */
public final class Primitives {
	private Primitives() {}
	
	private static final Set<HighType> PRIMITIVE;
	public static final HighType VOID = new PrimitiveType("void", Atom.unf, 0); // none
	// public static final Type DOUBLE = new PrimitiveType("double", AtomType.i64, 8, true, true);
	// public static final Type FLOAT = new PrimitiveType("float", AtomType.i32, 4, true, true);
	public static final HighType LONG = new PrimitiveType("long", Atom.i64, 8);
	public static final HighType INT = new PrimitiveType("int", Atom.i32, 4);
	public static final HighType SHORT = new PrimitiveType("short", Atom.i16, 2);
	public static final HighType BYTE = new PrimitiveType("byte", Atom.i8, 1);
	public static final HighType CHAR = new PrimitiveType("char", Atom.i8, 1);
	public static final HighType BOOL = new PrimitiveType("bool", Atom.i8, 1);
	
	static {
		Set<HighType> types = new HashSet<HighType>();
		types.add(VOID);
		// types.add(DOUBLE);
		// types.add(FLOAT);
		types.add(LONG);
		types.add(INT);
		types.add(SHORT);
		types.add(BYTE);
		types.add(CHAR);
		types.add(BOOL);
		
		PRIMITIVE = Collections.unmodifiableSet(types);
	}
	
	/**
	 * Checks if the string is a primitive type.
	 * @param value
	 * @return true if the string was a primitive type.
	 */
	public static boolean contains(String value) {
		for(HighType t : PRIMITIVE) {
			if(t.name().equals(value)) return true;
		}
		
		return false;
	}
	
	/**
	 * Get the primitive datatype with a specified name.
	 * @param value
	 * @return the primitive if found otherwise null.
	 */
	public static HighType getType(String value) {
		for(HighType t : PRIMITIVE) {
			if(t.name().equals(value)) return t;
		}
		
		return null;
	}
	
	/**
	 * Returns a unmodifiable set with all primitives inside of it.
	 * @return the set of primitives.
	 */
	public static Set<HighType> getAllTypes() {
		return PRIMITIVE;
	}
}
