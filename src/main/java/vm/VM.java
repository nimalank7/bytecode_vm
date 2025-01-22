package vm;

import java.util.ArrayList;
import java.util.List;

import static vm.Bytecode.BR;
import static vm.Bytecode.BRF;
import static vm.Bytecode.BRT;
import static vm.Bytecode.CALL;
import static vm.Bytecode.GLOAD;
import static vm.Bytecode.GSTORE;
import static vm.Bytecode.HALT;
import static vm.Bytecode.IADD;
import static vm.Bytecode.ICONST;
import static vm.Bytecode.IEQ;
import static vm.Bytecode.ILT;
import static vm.Bytecode.IMUL;
import static vm.Bytecode.ISUB;
import static vm.Bytecode.LOAD;
import static vm.Bytecode.POP;
import static vm.Bytecode.PRINT;
import static vm.Bytecode.RET;
import static vm.Bytecode.STORE;

/** A simple stack-based interpreter */
public class VM {
	public static final int DEFAULT_STACK_SIZE = 1000;
	public static final int DEFAULT_CALL_STACK_SIZE = 1000;
	public static final int FALSE = 0;
	public static final int TRUE = 1;

	// instruction pointer register
	int ip;
	// stack pointer register
	int sp = -1;
	// frame point register
	int fp = -1;

	// word-addressable code memory but still bytecodes.
	int[] code;
	// global variable space
	int[] globals;
	// Operand stack, grows upwards
	int[] stack;
	// Call stack, grows upwards
	Context[] callstack;
	// the currently executing function
	Context ctx;

	/** Metadata about the functions allows us to refer to functions by
	 * 	their index in this table. It makes code generation easier for
	 * 	the bytecode compiler because it doesn't have to resolve
	 *  addresses for forward references. It can generate simply
	 *  "CALL i" where i is the index of the function. Later, the
	 *  compiler can store the function address in the metadata table
	 *  when the code is generated for that function.
	 */
	FunctionMetaData[] metadata;

	public boolean trace = false;

	public VM(int[] code, int nGlobals, FunctionMetaData[] metadata) {
		this.code = code;
		this.globals = new int[nGlobals];
		this.stack = new int[DEFAULT_STACK_SIZE];
		this.metadata = metadata;
	}

	public void exec(int startip) {
		ip = startip;
		// Start the main() function
		ctx = new Context(null,0, metadata[0]);
		cpu();
	}

	/** Simulate the fetch-decode execute cycle */
	protected void cpu() {
		// Retrieve opcode from first instruction
		int opcode = code[ip];
		// Local variables to hold stack register values
		int a;
		int b;
		int addr;
		int regnum;

		// If opcode isn't HALT...
		while (opcode != HALT && ip < code.length) {
			if (trace) {
				System.err.printf("%-35s", disInstr());
			}
			// Jump to next instruction/operand
			ip++;
			switch (opcode) {
				case IADD:
					// 2nd opnd at top of stack
					b = stack[sp--];
					// 1st opnd 1 below top
					a = stack[sp--];
					// push result
					stack[++sp] = a + b;
					break;
				case ISUB:
					b = stack[sp--];
					a = stack[sp--];
					stack[++sp] = a - b;
					break;
				case IMUL:
					b = stack[sp--];
					a = stack[sp--];
					stack[++sp] = a * b;
					break;
				case ILT:
					b = stack[sp--];
					a = stack[sp--];
					// Push TRUE/FALSE onto stack
					stack[++sp] = (a < b) ? TRUE : FALSE;
					break;
				case IEQ:
					b = stack[sp--];
					a = stack[sp--];
					stack[++sp] = (a == b) ? TRUE : FALSE;
					break;
				case BR:
					ip = code[ip];
					break;
				case BRT:
					addr = code[ip++];
					if ( stack[sp--]==TRUE ) ip = addr;
					break;
				case BRF:
					addr = code[ip++];
					if ( stack[sp--]==FALSE ) ip = addr;
					break;
				case ICONST:
					// push operand
					stack[++sp] = code[ip++];
					break;
				// load local function variable or arg into stack
				case LOAD:
					// E.g. if LOAD 1 then increment IP to put 1 into regnum
					regnum = code[ip++];
					// Add to stack
					stack[++sp] = ctx.locals[regnum];
					break;
				// load from global memory
				case GLOAD:
					addr = code[ip++];
					stack[++sp] = globals[addr];
					break;
				case STORE:
					regnum = code[ip++];
					ctx.locals[regnum] = stack[sp--];
					break;
				case GSTORE:
					addr = code[ip++];
					globals[addr] = stack[sp--];
					break;
				case PRINT:
					// Print out current stack pointer then decrement it
					System.out.println(stack[sp--]);
					break;
				case POP:
					--sp;
					break;
				/*
					Before CALL the arguments for the function should be on the stack.
					CALL 1 - 1 represents the index of the function metadata array

					CALL opcode increments the instruction pointer so that it points to
					the instruction that represents the index position in the function metadata array.
					FuncMetadata[0] is the main function so FuncMetadata[1] gets the next function
				*/
				case CALL:
					/*
					Retrieve the instruction that points to the index position of the function metadata array
					(i.e. the function to call). Then increment the instruction pointer
					 */
					int functionIndex = code[ip++];
					// how many args the function has
					int nargs = metadata[functionIndex].nArgs;
					/*
					Create a new context which represents the function about to be executed (e.g. f()).

					ctx = the parent context. So if calling f() from main() then ctx = main
					which represents the parent context.
					ip = the location of the current instruction pointer which we will jump back to.
					metadata = function to be executed
					 */
					ctx = new Context(ctx, ip, metadata[functionIndex]);

					// Retrieve index of first function argument from stack
					int firstarg = sp - nargs + 1;

					/*
					Copy function arguments from stack into ctx.locals. Then move the stack pointer back
					the number of function arguments
					 */
					for (int i = 0; i < nargs; i++) {
						ctx.locals[i] = stack[firstarg + i];
					}

					/*
					Then move the stack pointer back the number of function arguments. As the function
					executes the function arguments will be loaded into the stack overwriting the values.
					 */
					sp -= nargs;

					// Jump to address where the function is located
					ip = metadata[functionIndex].address;
					break;
				case RET:
					// Set instruction pointer to the return instruction of the current function
					ip = ctx.returnIp;
					// Set current context to the parent function
					ctx = ctx.invokingContext;
					break;
				default:
					throw new Error("invalid opcode: "+opcode+" at ip="+(ip-1));
			}
			if (trace) {
				System.err.printf("%-22s %s\n", stackString(), callStackString());
			}

			// Retrieve opcode from instruction
			opcode = code[ip];
		}
		if (trace) {
			System.err.printf("%-35s", disInstr());
		}
		if (trace) {
			System.err.println(stackString());
		}
		if (trace) {
			dumpDataMemory();
		}
	}

	protected String stackString() {
		StringBuilder buf = new StringBuilder();
		buf.append("stack=[");
		for (int i = 0; i <= sp; i++) {
			int o = stack[i];
			buf.append(" ");
			buf.append(o);
		}
		buf.append(" ]");
		return buf.toString();
	}

	protected String callStackString() {
		List<String> stack = new ArrayList<>();
		Context c = ctx;
		while (c != null) {
			if (c.metadata != null) {
				stack.add(0, c.metadata.name);
			}
			c = c.invokingContext;
		}
		return "calls=" + stack;
	}

	protected String disInstr() {
		int opcode = code[ip];
		String opName = Bytecode.instructions[opcode].name;
		StringBuilder buf = new StringBuilder();
		buf.append(String.format("%04d:\t%-11s", ip, opName));
		int nargs = Bytecode.instructions[opcode].n;
		if (opcode == CALL) {
			buf.append(metadata[code[ip+1]].name);
		} else if (nargs > 0) {
			List<String> operands = new ArrayList<>();

			for (int i = ip + 1; i <= ip + nargs; i++) {
				operands.add(String.valueOf(code[i]));
			}

			for (int i = 0; i < operands.size(); i++) {
				String s = operands.get(i);
				if (i > 0) {
					buf.append(", ");
				}
				buf.append(s);
			}
		}
		return buf.toString();
	}

	protected void dumpDataMemory() {
		System.err.println("Data memory:");
		int addr = 0;
		for (int o : globals) {
			System.err.printf("%04d: %s\n", addr, o);
			addr++;
		}
		System.err.println();
	}

	protected void dumpCodeMemory() {
		System.err.println("Code memory:");
		int addr = 0;
		for (int o : code) {
			System.err.printf("%04d: %s\n", addr, o);
			addr++;
		}
		System.err.println();
	}
}
