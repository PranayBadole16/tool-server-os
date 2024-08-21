package in.javis.toolserver.service.executors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javis.universaltoolbridge.tools.ToolRegistry;
import in.javis.toolserver.pojo.ToolServerRequest;
import in.javis.toolserver.service.ToolManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class ScriptExecutorService {

    @Autowired
    ToolManagerService toolManagerService;

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
