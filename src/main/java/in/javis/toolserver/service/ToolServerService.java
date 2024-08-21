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

    public Object executeRequest(ToolServerRequest toolServerRequest) {

        String script = toolServerRequest.getScript();
        String toolName;
        boolean isScriptTool;
        Object result;

        if ((script != null) && !script.isEmpty()) {
            toolName = extractToolNameFromScript(script);
            isScriptTool = isScriptTool(toolName);
            log.warn("Calling Script Executor Service");
            result = scriptExecutorService.executeScript(toolServerRequest, isScriptTool);
        } else {
            toolName = toolServerRequest.getToolName();
            isScriptTool = isScriptTool(toolName);
            log.warn("Calling Tool Executor Service");
            result = toolExecutorService.executeTool(toolServerRequest, isScriptTool);
        }

        if (isScriptTool) {
            try {
                String jsonString = result.toString().replace("'", "\"");
                return new ObjectMapper().readTree(jsonString);
            } catch (JsonProcessingException jpe) {
                return result.toString();
            }
        } else {
            return convertPolyglotValue(result);
        }
    }

    public void embedPythonFiles(EmbedS3FileRequest request) throws IOException {
        for (EmbedS3FileRequest.Record record : request.getRecords()) {
            String bucketName = record.getBucket();
            String key = record.getKey();
            String toolName = Utils.getFileName(key);

            S3Object s3Object = awsServices.getS3Object(bucketName, key);

            ToolRegistry toolRegistry = toolManagerService.getToolRegistry();
            ScriptExecutor executor = toolManagerService.getExecutor();
            Context pythonContext = toolManagerService.getPythonContextHandler().getContext();

            Source source = Source.newBuilder(PYTHON.getName(), new InputStreamReader(s3Object.getObjectContent()), "").build();
            pythonContext.eval(source);

            List<String> arguments = (List<String>) convertPolyglotValue(pythonContext.eval(PYTHON.getName(), LIST_PYTHON_ARGS_SCRIPT.getName().formatted(toolName)));
            arguments.removeFirst();

            Tool newScriptTool = new SimpleMultiLanguageTool(toolName, PYTHON.getName(), pythonContext, arguments.toArray(new String[0]));
            toolRegistry.registerTool(toolName, newScriptTool);
            executor.embedScript(toolName, newScriptTool);
        }
    }


}
