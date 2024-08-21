package in.javis.toolserver.helpers;

import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.javis.universaltoolbridge.tools.ToolRegistry;
import in.javis.toolserver.constants.ToolEnum;
import in.javis.toolserver.pojo.S3FilterListResponse;
import org.apache.commons.io.FilenameUtils;

import java.util.*;

public class Utils {

    public static String extractToolNameFromScript(String script) {
        int openParenIndex = script.indexOf('(');
        return script.substring(0, openParenIndex).trim();
    }

    public static boolean isScriptTool(String toolName) {
        try {
            return ToolEnum.valueOf(toolName).isScriptTool();
        } catch (Exception e) {
            return true;
        }
    }

    public static S3FilterListResponse filterFileKeys(List<S3ObjectSummary> s3ObjectSummaries, ToolRegistry toolRegistry, Map<String, Date> toolLastUpdatedMap) {
        Set<String> allTools = new HashSet<>();
        List<String> addedKeys = new ArrayList<>();
        List<String> deletedTools = new ArrayList<>();

        for (S3ObjectSummary summary : s3ObjectSummaries) {
            String key = summary.getKey();
            String toolName = getFileName(key);

            allTools.add(toolName);

            if (key.endsWith(".py") &&
                    ((!toolRegistry.getTools().containsKey(toolName)) ||
                            isToolUpdated(toolName, toolLastUpdatedMap, summary.getLastModified()))) {
                addedKeys.add(key);
            }
        }

        toolLastUpdatedMap.keySet().forEach(tool -> {
            if (!allTools.contains(tool)) {
                deletedTools.add(tool);
            }
        });
        return new S3FilterListResponse(addedKeys, deletedTools);
    }

    private static boolean isToolUpdated(String toolName, Map<String, Date> toolLastUpdatedMap, Date latestModified) {
        return latestModified.after(toolLastUpdatedMap.get(toolName));
    }

    public static String getFileName(String filteredKey) {
        String[] parts = filteredKey.split("/");
        return FilenameUtils.removeExtension(parts[parts.length - 1]);
    }
}
