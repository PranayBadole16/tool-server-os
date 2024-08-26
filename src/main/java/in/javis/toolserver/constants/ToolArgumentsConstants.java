package in.javis.toolserver.constants;

import java.util.List;

/**
 * Enum for tool arguments used in the application.
 * <p>
 * This enum defines lists of string constants that specify the arguments
 * required for different tools within the application. It ensures consistency
 * by providing a centralized place for these argument definitions.
 * </p>
 */
public enum ToolArgumentsConstants {

    ADD_TOOL_ARGS(List.of("a", "b")),
    RESPONSE_TOOL_ARGS(List.of("text"));

    private final List<String> args;

    ToolArgumentsConstants(List<String> args) {
        this.args = args;
    }

    public List<String> getArgs() {
        return args;
    }
}
