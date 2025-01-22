package vm;

public class FunctionMetaData {
	public String name;
	// Number of arguments
	public int nArgs;
	// Number of local variables in function
	public int nLocals;
	// bytecode address of where the function is
	public int address;

	public FunctionMetaData(String name, int nArgs, int nLocals, int address) {
		this.name = name;
		this.nArgs = nArgs;
		this.nLocals = nLocals;
		this.address = address;
	}
}
