package in.javis.toolserver.service;

import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.javis.universaltoolbridge.context.ContextHandler;
import com.javis.universaltoolbridge.executor.ScriptExecutor;
import com.javis.universaltoolbridge.tools.SimpleMultiLanguageTool;
import com.javis.universaltoolbridge.tools.Tool;
import com.javis.universaltoolbridge.tools.ToolRegistry;
import in.javis.toolserver.config.aws.AwsServices;
import in.javis.toolserver.helpers.Utils;
import in.javis.toolserver.pojo.S3FilterListResponse;
import in.javis.toolserver.tools.generic.AddTool;
import in.javis.toolserver.tools.generic.ResponseTool;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static in.javis.toolserver.constants.StringEnum.LIST_PYTHON_ARGS_SCRIPT;
import static in.javis.toolserver.constants.StringEnum.PYTHON;
import static in.javis.toolserver.constants.ToolEnum.*;
import static in.javis.toolserver.helpers.PolyglotObjectMapper.convertPolyglotValue;

@Service
@Slf4j
public class ToolManagerService {

    @Getter
    private final ToolRegistry toolRegistry = new ToolRegistry();

    @Getter
    private final ToolRegistry formattedToolRegistry = new ToolRegistry();

    private final Map<String, Date> toolLastUpdatedMap = new HashMap<>();

    @Getter
    private ScriptExecutor executor;

    @Getter
    private final ContextHandler pythonContextHandler = new ContextHandler(PYTHON.getName());

    @Autowired
    private AwsServices awsServices;

    @PostConstruct
    private void initialize() {
        try {
            pythonContextHandler.getContext().eval(PYTHON.getName(), "import inspect, json");
            loadResourceFiles(pythonContextHandler, List.of("vfs/proj/multiply_script.py"/*, "vfs/proj/fetch_data_script.py", "vfs/proj/process_data_script.py"*/));

//            Value fetchDataFunction = pythonContextHandler.getContext().getBindings(PYTHON.getName()).getMember("fetch_data");
//
//            // Manually trigger embedded Python functions
//            org.graalvm.polyglot.Value result = fetchDataFunction.execute(null, "https://jsonplaceholder.typicode.com/todos/1");
//            System.out.println("Fetched data: " + result);

            Tool addTool = new AddTool();
            Tool multiplyTool = new SimpleMultiLanguageTool("multiply", PYTHON.getName(), pythonContextHandler.getContext(), "a", "b");

            toolRegistry.registerTool(ADD.getName(), addTool);
            toolRegistry.registerTool(MULTIPLY.getName(), multiplyTool);
            toolRegistry.registerTool(RESPONSE_TOOL.getName(), new ResponseTool());

            executor = new ScriptExecutor(PYTHON.getName(), toolRegistry);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void loadResourceFiles(ContextHandler contextHandler, List<String> filePaths) throws IOException {

        for (String filePath : filePaths) {
            try (InputStream is = ToolManagerService.class.getClassLoader().getResourceAsStream(filePath)) {
                if (is == null) throw new IOException("Resource " + filePath + " not found");

                try (InputStreamReader reader = new InputStreamReader(is)) {
                    Source source = Source.newBuilder(PYTHON.getName(), reader, "").build();
                    contextHandler.loadFile(source);
                }
            }
        }
    }

//    @Scheduled(fixedRate = 60 * 60 * 1000) //milliseconds
    private void embedFilesFromS3() throws IOException {
        String bucketName = "pranaytemp";
        String prefix = "tools/";

        Context pythonContext = pythonContextHandler.getContext();

        List<S3ObjectSummary> s3ObjectSummaries = awsServices.listAllObjectsOfBucketWithPrefix(bucketName, prefix);

        S3FilterListResponse response = Utils.filterFileKeys(s3ObjectSummaries, toolRegistry, toolLastUpdatedMap);
        List<String> addedKeys = response.getAddedKeys();
        List<String> deletedTools = response.getDeletedTools();

        for (String addedKey : addedKeys) {
            String toolName = Utils.getFileName(addedKey);

            try (S3Object s3Object = awsServices.getS3Object(bucketName, addedKey);
                 InputStreamReader reader = new InputStreamReader(s3Object.getObjectContent())) {

                toolLastUpdatedMap.put(toolName, s3Object.getObjectMetadata().getLastModified());
                log.info("Embedded Tool - {} into Python Context", toolName);

                Source source = Source.newBuilder(PYTHON.getName(), reader, "").build();
                pythonContext.eval(source);

                List<String> arguments = (List<String>) convertPolyglotValue(pythonContext.eval(PYTHON.getName(), LIST_PYTHON_ARGS_SCRIPT.getName().formatted(toolName)));
                arguments.removeFirst();

                Tool newScriptTool = new SimpleMultiLanguageTool(toolName, PYTHON.getName(), pythonContext, arguments.toArray(new String[0]));
                toolRegistry.registerTool(toolName, newScriptTool);
                executor.embedScript(toolName, newScriptTool);

            } catch (Exception e) {
                log.error("Error processing file {} from S3", addedKey, e);
            }
        }

        for (String toolName : deletedTools) {
            log.info("Removed Embedded Tool - {} from Python Context", toolName);
            toolRegistry.removeTool(toolName);
            toolLastUpdatedMap.remove(toolName);
            pythonContext.getBindings(PYTHON.getName()).removeMember(toolName);
            executor.removeTool(toolName);
        }
    }
}
