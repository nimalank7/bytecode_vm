package vm;

// (byte is signed; use a short to keep 0..255)
public class Bytecode {
	// Arithmetic instructions
	// int add
	public static final short IADD = 1;
	// int sub
	public static final short ISUB = 2;
	// int multiply
	public static final short IMUL = 3;
	// int less than
	public static final short ILT  = 4;
	// int equal
	public static final short IEQ  = 5;

	// Branching instructions
	// branch
	public static final short BR   = 6;
	// branch if true
	public static final short BRT  = 7;
	// branch if false
	public static final short BRF  = 8;
	// push constant integer
	public static final short ICONST = 9;

	// Local context instructions
	// load from local context
	public static final short LOAD   = 10;
	// store in local context
	public static final short STORE  = 12;

	// Global memory instructions
	// load from global memory
	public static final short GLOAD  = 11;
	// store in global memory
	public static final short GSTORE = 13;

	// Stack instructions
	// print stack top
	public static final short PRINT  = 14;
	// throw away top of stack
	public static final short POP  = 15;

	// Function instructions
	// Call a function
	public static final short CALL = 16;
	// return with/without value
	public static final short RET  = 17;

	// Stop
	public static final short HALT = 18;
	public static Instruction[] instructions = new Instruction[] {
			// <INVALID>
			null,
			// index is the opcode
			new Instruction("iadd"),
			new Instruction("isub"),
			new Instruction("imul"),
			new Instruction("ilt"),
			new Instruction("ieq"),
			new Instruction("br", 1),
			new Instruction("brt", 1),
			new Instruction("brf", 1),
			new Instruction("iconst", 1),
			new Instruction("load", 1),
			new Instruction("gload", 1),
			new Instruction("store", 1),
			new Instruction("gstore", 1),
			new Instruction("print"),
			new Instruction("pop"),
			// call index of function in meta-info table
			new Instruction("call", 2),
			new Instruction("ret"),
			new Instruction("halt")
	};

	public static class Instruction {
		// Name of bytecode (e.g. 'iadd', 'call')
		String name;
		// Number of arguments
		int n;

		public Instruction(String name) {
			this(name,0);
		}

		public Instruction(String name, int nargs) {
			this.name = name;
			this.n = nargs;
		}
	}
}
