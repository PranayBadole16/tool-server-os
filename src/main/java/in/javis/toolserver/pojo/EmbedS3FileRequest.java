package in.javis.toolserver.pojo;

import lombok.Data;

import java.util.List;

/**
 * Represents a request to embed files from S3.
 * <p>
 * This class encapsulates the details required to specify which S3 files should be embedded.
 * It contains a list of records, each specifying a bucket and a key for the file to be embedded.
 * </p>
 */
@Data
public class EmbedS3FileRequest {
    private List<Record> records;

    @Data
    public static class Record {
        private String bucket;
        private String key;
    }
}
