package in.javis.toolserver.helpers;

import org.graalvm.polyglot.Value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for converting GraalVM Polyglot {@link Value} objects to standard Java objects.
 * <p>
 * This class provides a method to recursively convert a GraalVM Polyglot {@link Value} into
 * a standard Java type such as {@link List}, {@link Map}, {@link String}, {@link Number}, or {@link Boolean}.
 * It handles conversion of arrays and objects, preserving their structure.
 * </p>
 */
public class PolyglotObjectMapper {

    /**
     * Converts a GraalVM Polyglot {@link Value} to a standard Java object.
     * <p>
     * This method checks the type of the {@link Value} and converts it to an appropriate Java type:
     * <ul>
     *     <li>Host objects are returned as-is.</li>
     *     <li>Strings are converted to {@link String}.</li>
     *     <li>Numbers are converted to {@link Number}.</li>
     *     <li>Booleans are converted to {@link Boolean}.</li>
     *     <li>Arrays are converted to {@link List} with recursively converted elements.</li>
     *     <li>Objects are converted to {@link Map} with recursively converted members.</li>
     * </ul>
     * </p>
     *
     * @param result the GraalVM Polyglot {@link Value} to be converted
     * @return the converted Java object
     */
    public static Object convertPolyglotValue(Object result) {

        if (result instanceof Value value) {

            if (value.isHostObject()) {
                // Return the host object directly
                return value.asHostObject();
            }
            if (value.isString()) {
                // Convert to Java String
                return value.asString();

            } else if (value.isNumber()) {
                // Convert to Java Number
                return value.as(Number.class);

            } else if (value.isBoolean()) {
                // Convert to Java Boolean
                return value.asBoolean();

            } else if (value.hasArrayElements()) {
                // Convert Polyglot array to Java List
                int arraySize = (int) value.getArraySize();
                List<Object> list = new ArrayList<>();
                for (int i = 0; i < arraySize; i++) {
                    list.add(convertPolyglotValue(value.getArrayElement(i)));
                }
                return list;

            } else if (value.hasMembers()) {
                // Convert Polyglot object to Java Map
                Map<String, Object> map = new HashMap<>();
                for (String key : value.getMemberKeys()) {
                    map.put(key, convertPolyglotValue(value.getMember(key)));
                }
                return map;
            }
        }

        // Return as-is if not a Polyglot Value
        return result;
    }
}
