package in.javis.toolserver.tools.generic;

import com.javis.universaltoolbridge.tools.AbstractTool;
import org.graalvm.polyglot.Value;

import static in.javis.toolserver.constants.ToolArgumentsConstants.ADD_TOOL_ARGS;

/**
 * Example tool implementation that adds two integers.
 * Extends AbstractTool to inherit common functionality.
 */
public class AddTool extends AbstractTool {

    /**
     * Constructs an AddTool instance and initializes argument names.
     * Adds "a" and "b" as expected argument names.
     */
    public AddTool() {
        this.argumentNames = ADD_TOOL_ARGS.getArgs();
    }

    /**
     * Executes the addition operation based on provided arguments.
     *
     * @param args the arguments to be added, expected as Value objects.
     * @return the sum of the two integers if executionParams equals 123, otherwise 0.
     */
    @Override
    public Object execute(Value... args) {
        Integer a = args[0].asInt();
        Integer b = args[1].asInt();
        return add(a, b);
    }

    /**
     * Performs addition of two integers based on executionParams.
     *
     * @param a the first integer to add.
     * @param b the second integer to add.
     * @return the sum of integers if executionParams equals 123, otherwise 0.
     */
    private Integer add(Integer a, Integer b) {
        if ((Integer) executionParams == 123)
            return a + b;
        else {
            return 0;
        }
    }
}
