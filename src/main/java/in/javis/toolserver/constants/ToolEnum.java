package in.javis.toolserver.constants;

import lombok.Getter;

/**
 * Enum representing different tools with associated properties.
 * <p>
 * This enum defines a set of tool types, each with a name and a flag isScriptTool indicating whether it is embedded from a Python script.
 * It provides methods to access these properties.
 * </p>
 */
public enum ToolEnum {

    ADD("ADD", false),
    MULTIPLY("MULTIPLY", true),
    RESPONSE_TOOL("RESPONSE_TOOL", false),
    ;

    @Getter
    private final String name;
    private final boolean isScriptTool;

    ToolEnum(String name, boolean isScriptTool) {
        this.name = name;
        this.isScriptTool = isScriptTool;
    }

    public boolean isScriptTool() {
        return this.isScriptTool;
    }

}
