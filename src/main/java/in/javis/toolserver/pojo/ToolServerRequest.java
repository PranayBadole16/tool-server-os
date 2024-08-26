package in.javis.toolserver.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Represents a request to the tool server.
 * <p>
 * This class encapsulates all the necessary information required to execute a tool request on the server.
 * It includes details such as the tool name, parameters, language, script, token, and
 * context (The context or additional information needed at runtime by the tool's script.).
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ToolServerRequest {
    private String toolName;
    private Map<String, Object> toolParams;
    private String language;
    private String script;
    private String token;
    private Object context;

    @Override
    public String toString() {
        return "ToolServerRequest{" +
                ", toolName='" + toolName + '\'' +
                ", toolParams=" + toolParams +
                ", language='" + language + '\'' +
                ", script='" + script + '\'' +
                ", token='" + token + '\'' +
                '}';
    }
}
