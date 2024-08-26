package in.javis.toolserver.service.executors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javis.universaltoolbridge.tools.Tool;
import com.javis.universaltoolbridge.tools.ToolRegistry;
import in.javis.toolserver.pojo.ToolServerRequest;
import in.javis.toolserver.security.JWTUtil;
import in.javis.toolserver.service.ToolManagerService;
import lombok.extern.slf4j.Slf4j;
import org.graalvm.polyglot.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static in.javis.toolserver.constants.ToolEnum.RESPONSE_TOOL;

/**
 * Service class responsible for executing tools based on the provided ToolServerRequest and context.
 * <p>
 * This service interacts with the ToolManagerService to retrieve the ToolRegistry and execute tools with the
 * specified parameters. It handles both script and non-script tools, verifying the request and populating
 * arguments accordingly.
 * </p>
 *
 * <p>
 * In case of an error during tool execution, the error is logged and `null` is returned. If the requested tool
 * is not available or required parameters are missing, a default tool is called to handle the request.
 * </p>
 */
@Service
@Slf4j
public class ToolExecutorService {

    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private ToolManagerService toolManagerService;

    /**
     * Executes a tool based on the given ToolServerRequest and context.
     *
     * <p>
     * The method retrieves the ToolRegistry from the ToolManagerService and sets the execution parameters based
     * on the provided context. It verifies the request for tool execution, populates the tool arguments, and then
     * executes the specified tool. If verification fails or an error occurs, a default tool is called.
     * </p>
     *
     * @param request the ToolServerRequest containing the tool name, parameters, and context information.
     * @param isScriptTool a boolean indicating if the tool is categorized as a "script tool".
     * @return the result of the tool execution or `null` if an error occurs.
     */
    public Object executeTool(ToolServerRequest request, boolean isScriptTool) {

        try {
            List<Value> argsValue = new ArrayList<>();
            ToolRegistry registry = toolManagerService.getToolRegistry();
            String toolName = request.getToolName();

            log.info("Tool Called - {}", toolName);

            var toolContext = isScriptTool ? new ObjectMapper().writeValueAsString(request.getContext()) : request.getContext();
            registry.setExecutionParams(toolContext);

            if (!verifyRequestForToolExecution(request, registry)) {
                return callDefaultTool(request, registry);
            }
            populateToolArguments(request, registry, argsValue);

            return registry.getTools().get(toolName).execute(argsValue.toArray(new Value[0]));
        } catch (Exception e) {
            log.error("Error While executing Tool - {}", e.getMessage());
        }
        return null;
    }

    /**
     * Calls the default tool when the requested tool is not verified for execution.
     *
     * <p>
     * The default tool is defined by the RESPONSE_TOOL constant. It executes with the provided tool parameters
     * and context.
     * </p>
     *
     * @param request the ToolServerRequest containing the tool parameters and context.
     * @param registry the ToolRegistry containing available tools.
     * @return the result of executing the default tool.
     */
    private Object callDefaultTool(ToolServerRequest request, ToolRegistry registry) {
        log.info("Calling Default Tool");
        String defaultToolName = RESPONSE_TOOL.getName();
        return registry.getTools().get(defaultToolName).execute(Value.asValue(request.getToolParams().toString()), Value.asValue(request.getContext()));
    }

    /**
     * Verifies if the request is valid for executing the specified tool.
     *
     * <p>
     * The method checks if the requested tool exists and if all required arguments are present in the request.
     * </p>
     *
     * @param request the ToolServerRequest containing the tool name and parameters.
     * @param registry the ToolRegistry containing available tools.
     * @return `true` if the tool exists and all required arguments are provided; otherwise, `false`.
     */
    private boolean verifyRequestForToolExecution(ToolServerRequest request, ToolRegistry registry) {
        String toolName = request.getToolName();
        Map<String, Tool> availableTools = registry.getTools();

        if (!availableTools.containsKey(toolName)) {
            log.info("Tool not found - {}", toolName);
            return false;
        }

        Tool tool = availableTools.get(toolName);
        List<String> requiredArgumentNames = tool.getArgumentNames();
        Map<String, Object> incomingArguments = request.getToolParams();

        for (String requiredArgumentName : requiredArgumentNames) {
            if (!incomingArguments.containsKey(requiredArgumentName)) {
                log.info("Incoming Request does not contain the required Tool Param - {} for ToolName - {}", requiredArgumentName, toolName);
                return false;
            }
        }

        return true;
    }

    /**
     * Populates the arguments for the specified tool based on the request parameters.
     *
     * <p>
     * The method extracts the required arguments from the request and converts them to `Value` objects for tool execution.
     * </p>
     *
     * @param request the ToolServerRequest containing the tool parameters.
     * @param registry the ToolRegistry containing available tools.
     * @param argsValue the list to be populated with tool arguments as `Value` objects.
     */
    private void populateToolArguments(ToolServerRequest request, ToolRegistry registry, List<Value> argsValue) {
        Tool tool = registry.getTools().get(request.getToolName());
        List<String> requiredArgumentNames = tool.getArgumentNames();
        Map<String, Object> incomingArguments = request.getToolParams();

        for (String requiredArgumentName : requiredArgumentNames) {
            argsValue.add(Value.asValue(incomingArguments.get(requiredArgumentName)));
        }
    }
}
