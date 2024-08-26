package in.javis.toolserver.constants;

/**
 * Enumeration for various string constants used in the application.
 */
public enum StringEnum {
    PYTHON("python"),
    LIST_PYTHON_ARGS_SCRIPT("list(inspect.signature(%s).parameters.keys())"),
    ;
    private final String name;

    StringEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
