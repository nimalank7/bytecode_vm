package vm;

/** To call, push one of these and pop to return */
public class Context {
	// parent function that called this function (e.g. 'caller')
	Context invokingContext;
	// Current function we are executing
	FunctionMetaData metadata;
	// Instruction pointer to return to
	int returnIp;
	// args + locals, indexed from 0
	int[] locals;

	public Context(Context invokingContext, int returnIp, FunctionMetaData metadata) {
		this.invokingContext = invokingContext;
		this.returnIp = returnIp;
		this.metadata = metadata;
		this.locals = new int[metadata.nArgs + metadata.nLocals];
	}
}
