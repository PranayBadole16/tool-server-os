package in.javis.toolserver.service;

import com.amazonaws.services.s3.model.S3Object;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javis.universaltoolbridge.executor.ScriptExecutor;
import com.javis.universaltoolbridge.tools.SimpleMultiLanguageTool;
import com.javis.universaltoolbridge.tools.Tool;
import com.javis.universaltoolbridge.tools.ToolRegistry;
import in.javis.toolserver.config.aws.AwsServices;
import in.javis.toolserver.helpers.Utils;
import in.javis.toolserver.pojo.EmbedS3FileRequest;
import in.javis.toolserver.pojo.ToolServerRequest;
import in.javis.toolserver.service.executors.ScriptExecutorService;
import in.javis.toolserver.service.executors.ToolExecutorService;
import lombok.extern.slf4j.Slf4j;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import static in.javis.toolserver.constants.StringEnum.LIST_PYTHON_ARGS_SCRIPT;
import static in.javis.toolserver.constants.StringEnum.PYTHON;
import static in.javis.toolserver.helpers.PolyglotObjectMapper.convertPolyglotValue;
import static in.javis.toolserver.helpers.Utils.extractToolNameFromScript;
import static in.javis.toolserver.helpers.Utils.isScriptTool;

/**
 * Service class responsible for handling tool and script execution requests, as well as embedding Python scripts from AWS S3.
 * <p>
 * This service coordinates the execution of tool and script requests by delegating to appropriate executor services,
 * and it supports embedding Python scripts into the tool registry from S3. It handles both script execution and tool
 * execution based on the type of request received.
 * </p>
 */
@Service
@Slf4j
public class ToolServerService {

    @Autowired
    ToolExecutorService toolExecutorService;

    @Autowired
    ScriptExecutorService scriptExecutorService;

    @Autowired
    ToolManagerService toolManagerService;

    @Autowired
    AwsServices awsServices;

    /**
     * Executes a request to either run a tool or a script based on the provided ToolServerRequest.
     * <p>
     * This method determines whether the request is for a script or a tool based on the presence of a script. It then
     * delegates the execution to the appropriate service. After execution, if the result is from a script, it attempts to
     * parse the result as JSON. If the result is from a tool, it converts the result using the PolyglotObjectMapper.
     * </p>
     *
     * @param toolServerRequest the request containing details about the tool or script to execute.
     * @return the result of the tool or script execution, parsed as JSON if the request was for a script, or as a Polyglot value.
     */
    public Object executeRequest(ToolServerRequest toolServerRequest) {

        String script = toolServerRequest.getScript();
        String toolName;
        boolean isScriptTool;
        Object result;

        if ((script != null) && !script.isEmpty()) {
            // Determine that this is a script execution request
            toolName = extractToolNameFromScript(script);
            isScriptTool = isScriptTool(toolName);
            log.warn("Calling Script Executor Service");
            result = scriptExecutorService.executeScript(toolServerRequest, isScriptTool);
        } else {
            // Determine that this is a tool execution request
            toolName = toolServerRequest.getToolName();
            isScriptTool = isScriptTool(toolName);
            log.warn("Calling Tool Executor Service");
            result = toolExecutorService.executeTool(toolServerRequest, isScriptTool);
        }

        if (isScriptTool) {
            try {
                // Attempt to parse the result as JSON if it is from a script
                String jsonString = result.toString().replace("'", "\"");
                return new ObjectMapper().readTree(jsonString);
            } catch (JsonProcessingException jpe) {
                // Return the result as a string if JSON parsing fails
                return result.toString();
            }
        } else {
            // Convert the result to a Polyglot value if it is from a tool
            return convertPolyglotValue(result);
        }
    }

    /**
     * Embeds Python script files into the tool registry from AWS S3 based on the provided request.
     * <p>
     * This method processes each record in the request to fetch the Python script from S3, evaluates it in the Python
     * context, and registers the script as a tool in the tool registry. It also updates the ScriptExecutor with the new
     * tool.
     * </p>
     *
     * @param request the request containing details about the S3 files to embed.
     * @throws IOException if an error occurs while accessing S3 or processing the script files.
     */
    public void embedPythonFiles(EmbedS3FileRequest request) throws IOException {
        for (EmbedS3FileRequest.Record record : request.getRecords()) {
            String bucketName = record.getBucket();
            String key = record.getKey();
            String toolName = Utils.getFileName(key);

            // Fetch the Python script from S3
            S3Object s3Object = awsServices.getS3Object(bucketName, key);

            ToolRegistry toolRegistry = toolManagerService.getToolRegistry();
            ScriptExecutor executor = toolManagerService.getExecutor();
            Context pythonContext = toolManagerService.getPythonContextHandler().getContext();

            // Build and evaluate the script from S3 in the Python context
            Source source = Source.newBuilder(PYTHON.getName(), new InputStreamReader(s3Object.getObjectContent()), "").build();
            pythonContext.eval(source);

            // Extract the list of arguments for the tool from the Python context
            List<String> arguments = (List<String>) convertPolyglotValue(pythonContext.eval(PYTHON.getName(), LIST_PYTHON_ARGS_SCRIPT.getName().formatted(toolName)));
            arguments.removeFirst(); // Remove the first argument if necessary

            // Create and register the new tool in the local registry and executor
            Tool newScriptTool = new SimpleMultiLanguageTool(toolName, PYTHON.getName(), pythonContext, arguments.toArray(new String[0]));
            toolRegistry.registerTool(toolName, newScriptTool);
            executor.embedScript(toolName, newScriptTool);
        }
    }
}
