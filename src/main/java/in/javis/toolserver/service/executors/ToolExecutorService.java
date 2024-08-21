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

@Service
@Slf4j
public class ToolExecutorService {

    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    ToolManagerService toolManagerService;

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

    private Object callDefaultTool(ToolServerRequest request, ToolRegistry registry) {
        log.info("Calling Default Tool");
        String defaultToolName = RESPONSE_TOOL.getName();
        return registry.getTools().get(defaultToolName).execute(Value.asValue(request.getToolParams().toString()), Value.asValue(request.getContext()));
    }

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

    private void populateToolArguments(ToolServerRequest request, ToolRegistry registry, List<Value> argsValue) {
        Tool tool = registry.getTools().get(request.getToolName());
        List<String> requiredArgumentNames = tool.getArgumentNames();
        Map<String, Object> incomingArguments = request.getToolParams();

        for (String requiredArgumentName : requiredArgumentNames) {
            argsValue.add(Value.asValue(incomingArguments.get(requiredArgumentName)));
        }
    }
}
