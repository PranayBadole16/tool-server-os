package in.javis.toolserver.helpers;

import org.graalvm.polyglot.Value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PolyglotObjectMapper {
    public static Object convertPolyglotValue(Object result) {

        if (result instanceof Value value) {

            if (value.isHostObject()) {
                return value.asHostObject();
            }
            if (value.isString()) {
                return value.asString();

            } else if (value.isNumber()) {
                return value.as(Number.class);

            } else if (value.isBoolean()) {
                return value.asBoolean();

            } else if (value.hasArrayElements()) {
                int arraySize = (int) value.getArraySize();
                List<Object> list = new ArrayList<>();
                for (int i = 0; i < arraySize; i++) {
                    list.add(convertPolyglotValue(value.getArrayElement(i)));
                }
                return list;

            } else if (value.hasMembers()) {
                Map<String, Object> map = new HashMap<>();
                for (String key : value.getMemberKeys()) {
                    map.put(key, convertPolyglotValue(value.getMember(key)));
                }
                return map;
            }
        }

        return result;
    }
}
