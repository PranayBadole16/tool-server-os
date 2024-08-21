package in.javis.toolserver.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class S3FilterListResponse {

    private List<String> addedKeys;
    private List<String> deletedTools;
}
