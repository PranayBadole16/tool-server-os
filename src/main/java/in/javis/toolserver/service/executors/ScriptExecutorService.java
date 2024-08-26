package in.javis.toolserver.service.executors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javis.universaltoolbridge.tools.ToolRegistry;
import in.javis.toolserver.pojo.ToolServerRequest;
import in.javis.toolserver.service.ToolManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * Service class responsible for executing scripts using the provided ToolServerRequest and context.
 * <p>
 * This service interacts with the ToolManagerService to fetch the ToolRegistry and use it to execute the given script.
 * The context for the script execution is set based on whether the script is classified as a "script tool" or not.
 * </p>
 *
 * <p>
 * In case of an error during script execution, a generic error message is returned.
 * </p>
 */
@Service
public class ScriptExecutorService {

    @Autowired
    private ToolManagerService toolManagerService;

    /**
     * Executes a script with the given ToolServerRequest and context.
     *
     * @param request the ToolServerRequest containing the script and context information.
     * @param isScriptTool a boolean indicating if the script is categorized as a "script tool".
     * @return the result of the script execution or an error message if an exception occurs during the process.
     */
    public Object executeScript(ToolServerRequest request, boolean isScriptTool) {
        try {
            ToolRegistry registry = toolManagerService.getToolRegistry();
            String script = request.getScript();

            var toolContext = isScriptTool ? new ObjectMapper().writeValueAsString(request.getContext()) : request.getContext();
            registry.setExecutionParams(toolContext);
            return toolManagerService.getExecutor().executeScript(script, true);
        } catch (Exception e) {
            return "Unable to Process this Request";
        }
    }
}

