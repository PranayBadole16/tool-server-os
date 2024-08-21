package in.javis.toolserver.constants;

import lombok.Getter;

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
