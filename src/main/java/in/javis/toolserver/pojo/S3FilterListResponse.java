package in.javis.toolserver.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * Represents the response containing the results of filtering S3 object keys.
 * <p>
 * This class provides two lists: one for keys of newly added S3 objects and another for
 * tools that have been deleted based on the filtering criteria.
 * </p>
 */
@AllArgsConstructor
@Getter
public class S3FilterListResponse {

    private List<String> addedKeys;
    private List<String> deletedTools;
}
