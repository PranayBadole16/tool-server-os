package in.javis.toolserver.helpers;

import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.javis.universaltoolbridge.tools.ToolRegistry;
import in.javis.toolserver.constants.ToolEnum;
import in.javis.toolserver.pojo.S3FilterListResponse;
import org.apache.commons.io.FilenameUtils;

import java.util.*;

/**
 * Utility class providing various helper methods for tool and file operations.
 * <p>
 * This class includes methods for extracting tool names, checking if a tool is a script tool,
 * filtering S3 object summaries, and extracting file names from S3 keys.
 * </p>
 */
public class Utils {

    /**
     * Extracts the tool name from a given script string.
     * <p>
     * Assumes the tool name is the substring before the first parenthesis in the script string.
     * </p>
     *
     * @param script the script string containing the tool name
     * @return the extracted tool name
     */
    public static String extractToolNameFromScript(String script) {
        int openParenIndex = script.indexOf('(');
        return script.substring(0, openParenIndex).trim();
    }

    /**
     * Determines if a tool is a script tool based on its name.
     * <p>
     * Uses the {@link ToolEnum} to check if the tool is categorized as a script tool. If the tool name
     * is not found in the enum, it assumes the tool is a script tool (embedded at runtime from s3).
     * </p>
     *
     * @param toolName the name of the tool
     * @return {@code true} if the tool is a script tool, {@code false} otherwise
     */
    public static boolean isScriptTool(String toolName) {
        try {
            return ToolEnum.valueOf(toolName).isScriptTool();
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Filters S3 object summaries to identify newly added or deleted tool files.
     * <p>
     * This method compares S3 object summaries against the existing tool registry and last updated map
     * to determine which files are new or which tools have been removed.
     * </p>
     *
     * @param s3ObjectSummaries the list of S3 object summaries to filter
     * @param toolRegistry the tool registry containing known tools
     * @param toolLastUpdatedMap a map of tool names to their last update timestamps
     * @return an {@link S3FilterListResponse} containing lists of added and deleted keys
     */
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

    /**
     * Checks if a tool has been updated based on its last modified date.
     * <p>
     * Compares the latest modification date of the tool file with the stored last updated date.
     * </p>
     *
     * @param toolName the name of the tool
     * @param toolLastUpdatedMap a map of tool names to their last update timestamps
     * @param latestModified the latest modification date of the tool file
     * @return {@code true} if the tool has been updated, {@code false} otherwise
     */
    private static boolean isToolUpdated(String toolName, Map<String, Date> toolLastUpdatedMap, Date latestModified) {
        return latestModified.after(toolLastUpdatedMap.get(toolName));
    }

    /**
     * Extracts the file name from an S3 object key.
     * <p>
     * The file name is obtained by splitting the key by slashes and removing the file extension.
     * </p>
     *
     * @param filteredKey the S3 object key
     * @return the file name without extension
     */
    public static String getFileName(String filteredKey) {
        String[] parts = filteredKey.split("/");
        return FilenameUtils.removeExtension(parts[parts.length - 1]);
    }
}
