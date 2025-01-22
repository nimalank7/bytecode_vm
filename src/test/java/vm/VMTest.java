package vm;

import org.junit.jupiter.api.Test;

import static vm.Bytecode.*;

class VMTest {

    VM vm;

    @Test
    void helloFunction() {
        int[] hello = {
                ICONST, 1,
                ICONST, 2,
                IADD,
                PRINT,
                HALT
        };

        FunctionMetaData[] hello_metadata = {
                new FunctionMetaData("main", 0, 0, 0)
        };

        // Print out 3
        VM vm = new VM(hello, 0, hello_metadata);
        vm.trace = true;
        vm.exec(hello_metadata[0].address);
    }

    @Test
    void loopFunction() {
        int[] loop = {
                // .GLOBALS 2; N, I
                // N = 10						ADDRESS
                ICONST, 10,                // 0
                GSTORE, 0,                // 2
                // I = 0
                ICONST, 0,                // 4
                GSTORE, 1,                // 6
                // WHILE I<N:
                // START (8):
                GLOAD, 1,                // 8
                GLOAD, 0,                // 10
                ILT,                    // 12
                BRF, 24,                // 13
                //     I = I + 1
                GLOAD, 1,                // 15
                ICONST, 1,                // 17
                IADD,                    // 19
                GSTORE, 1,                // 20
                BR, 8,                    // 22
                // DONE (24):
                // PRINT "LOOPED "+N+" TIMES."
                HALT                    // 24
        };

        FunctionMetaData[] loop_metadata = {
                new FunctionMetaData("main", 0, 0, 0)
        };

        // What does loop do?
        VM vm = new VM(loop, 0, loop_metadata);
        vm.trace = true;
        vm.exec(loop_metadata[0].address);
    }

    @Test
    public void factorialFunction() {
        int FACTORIAL_INDEX = 1;
        int FACTORIAL_ADDRESS = 0;
        int MAIN_ADDRESS = 21;
        int[] factorial = {
                //.def factorial: ARGS=1, LOCALS=0	ADDRESS
                //	IF N < 2 RETURN 1
                LOAD, 0,                // 0
                ICONST, 2,                // 2
                ILT,                    // 4
                BRF, 10,                // 5
                ICONST, 1,                // 7
                RET,                    // 9
                //CONT:
                //	RETURN N * FACT(N-1)
                LOAD, 0,                // 10
                LOAD, 0,                // 12
                ICONST, 1,                // 14
                ISUB,                    // 16
                CALL, FACTORIAL_INDEX,    // 17
                IMUL,                    // 19
                RET,                    // 20
                //.DEF MAIN: ARGS=0, LOCALS=0
                // PRINT FACT(1)
                ICONST, 5,                // 21    <-- MAIN METHOD!
                CALL, FACTORIAL_INDEX,    // 23
                PRINT,                    // 25
                HALT                    // 26
        };

        FunctionMetaData[] factorial_metadata = {
                //.def factorial: ARGS=1, LOCALS=0	ADDRESS
                new FunctionMetaData("main", 0, 0, MAIN_ADDRESS),
                new FunctionMetaData("factorial", 1, 0, FACTORIAL_ADDRESS)
        };

        // 5! = 120
        VM vm = new VM(factorial, 0, factorial_metadata);
        vm.trace = true;
        vm.exec(factorial_metadata[0].address);
    }

    @Test
    public void multipleFunction() {
        int[] f = {
                /*
                main() {
                    print f(10);
                }
                 */
                ICONST, 10,                    // Address: 0
                CALL, 1,                    // Address: 2
                PRINT,                        // Address: 4
                HALT,                        // Address: 5

                /*
                f(x) starts at address 6

                ARGS = 1
                LOCALS = 1

                f(x) {
                    a = x;
                    return 2 * a;
                }
                 */

                //  a = x;
                LOAD, 0,                    // 6	<-- start of f
                STORE, 1,
                // return 2*a
                LOAD, 1,
                ICONST, 2,
                IMUL,
                RET
        };

        /*
        FuncMetaData[0] = main
        FuncMetaData[1] = f

        f's instructions are at address 6
        */
        FunctionMetaData[] f_metadata = {
                new FunctionMetaData("main", 0, 0, 0),
                new FunctionMetaData("f", 1, 1, 6)
        };

        // Print out 20
        VM vm = new VM(f, 2, f_metadata);
        vm.trace = true;
        vm.exec(f_metadata[0].address);
        vm.dumpDataMemory();
    }
}
